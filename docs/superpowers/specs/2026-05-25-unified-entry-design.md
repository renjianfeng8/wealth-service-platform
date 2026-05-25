# 单一域名统一入口设计

> **目标：** 用户只需访问 `rjfwealth.cn` 一个域名，无需区分 3000/3001 端口，系统根据用户角色自动路由到对应的 SPA。

**设计原则：** 不改现有两个 SPA（`front`、`front-user`）的内部代码，通过 nginx 路径分发 + 新增 thin SPA + 后端统一登录接口实现。

**架构摘要：**
```
rjfwealth.cn/
  ├─ /           → thin SPA（公开内容 + 统一登录）
  ├─ /login      → 统一登录页（在 thin SPA 中）
  ├─ /admin/*    → 管理后台 SPA（现有 front，改 base 路径）
  ├─ /user/*     → 用户前台 SPA（现有 front-user，改 base 路径）
  └─ /api/v1/*   → gateway → wealth-service（不变）
```

---

## 1. 路由拓扑

### 改之前

| 路径 | 目标 | 说明 |
|------|------|------|
| `/` | 管理后台 SPA | 入口一直指向后台 |
| `/user-portal/` | 用户前台 SPA | 子路径隔离 |
| `/api/v1/*` | gateway:8080 | API 代理 |

### 改之后

| 路径 | 目标 | 说明 |
|------|------|------|
| `/` | thin SPA (landing) | 公开产品/行情/资讯 + 统一登录入口 |
| `/login` | thin SPA (landing) | 统一登录页 |
| `/admin/*` | 管理后台 SPA (front) | 原 `/` 迁移至此 |
| `/user/*` | 用户前台 SPA (front-user) | 原 `/user-portal/` 改为 `/user/` |
| `/api/v1/*` | gateway:8080 | 不动 |

### 用户流程

```
用户访问 rjfwealth.cn
  │
  ├─ 未登录 → 看到产品列表、实时行情、财经资讯（公开 API）
  │
  ├─ 点击「登录」→ /login
  │     │
  │     ├─ 调 POST /user/identify-login
  │     │     ├─ 匹配 ums_admin 表 → userType=admin
  │     │     └─ 匹配 user 表      → userType=user
  │     │
  │     ├─ userType=admin → window.location = /admin/（整页跳转，加载后台 SPA）
  │     │
  │     └─ userType=user  → window.location = /user/（整页跳转，加载前台 SPA）
  │
  ├─ 直接访问 /admin/* → 加载管理后台 SPA，内部保持自己的登录逻辑
  │
  └─ 直接访问 /user/*  → 加载用户前台 SPA，内部保持自己的登录逻辑
```

---

## 2. 后端改动

### 2.1 新增统一登录接口

**Controller:** `UserController.java`（或新建 `UnifiedAuthController.java`）

```
POST /user/identify-login
Content-Type: application/json

Request:  { "username": "xxx", "password": "xxx" }
Response: {
    "code": 200,
    "data": {
        "token": "jwt...",
        "userType": "admin" | "user",
        "redirectUrl": "/admin" | "/user",
        "nickname": "xxx",
        "userId": 1
    }
}
```

**逻辑：** `UserServiceImpl.identifyLogin(LoginDTO dto)`

1. 先在 `ums_admin` 表按 username 查 → 若找到且密码匹配 → `userType=admin`
2. 没找到再在 `user` 表按 username 查 → 若找到且密码匹配 → `userType=user`
3. 都没找到 → throw `ServiceException(401, "用户名或密码错误")`
4. 生成 JWT，在 claims 中存入 `userType` 字段

### 2.2 JWT Payload 扩展

现有 token 的 claims：
```json
{
  "jti": "uuid",
  "sub": "username",
  "exp": 1234567890
}
```

改为：
```json
{
  "jti": "uuid",
  "sub": "username",
  "userType": "admin | user",
  "exp": 1234567890
}
```

JwtUtil 增加 `generateToken(String username, String userType)` 重载方法。

### 2.3 新文件/改动清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `UserController.java` | 新增 | `identify-login` 端点 |
| `UserService.java` | 新增接口 | `identifyLogin(LoginDTO)` |
| `UserServiceImpl.java` | 新增实现 | 双表查询 + JWT 生成 |
| `JwtUtil.java` | 修改 | 新增 `generateToken(username, userType)` 重载 |

### 2.4 不变的部分

- `/system/umsAdmin/login` 保留（后台 SPA 内部认证）
- `/user/login` 保留（前台 SPA 内部认证）
- `LoginInterceptor.java` 不变
- `JwtAuthGlobalFilter.java` 不变

---

## 3. 前端改动

### 3.1 新建 thin SPA（front-landing）

一个极简的 Vue 3 + Vite 项目，与 `front`、`front-user` 完全独立。

**文件结构：**
```
front-landing/
├── index.html
├── package.json
├── vite.config.ts
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── router/index.ts
│   ├── views/
│   │   ├── home/index.vue       # 公开首页：产品列表 + 行情 + 资讯
│   │   └── login/index.vue      # 统一登录页
│   ├── api/
│   │   └── index.ts             # axios 实例（baseURL: /api/v1）
│   │   └── user.ts              # identifyLogin() 接口
│   └── utils/
│       └── auth.ts              # token 存取
```

**路由：**
```ts
const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]
```

**登录页逻辑：**
```ts
async function handleLogin() {
  const res = await identifyLogin({ username, password })
  const { userType, token, redirectUrl } = res.data
  localStorage.setItem('wealth_token', token)  // 或通过 cookie
  // 整页跳转，加载对应 SPA
  window.location.href = redirectUrl
}
```

**首页公开内容（调现有 API）：**
- 产品列表：`GET /product/wea-product/page?pageNum=1&pageSize=10`
- 实时行情：`GET /product/wea-market-data/list`
- 财经资讯：`GET /message/news/page?pageNum=1&pageSize=5`

**样式方案：** 复用现有主题风格（深蓝 #1a365d 系），不引入 Element Plus 以外的 UI 框架。

### 3.2 管理后台 SPA（front）base 路径迁移

**改动文件：**

| 文件 | 改动 |
|------|------|
| `front/vite.config.ts` | 加 `base: '/admin/'` |

两个 SPA 均使用 `createWebHashHistory()`（hash 路由），URL hash 不会发送到服务端。因此**无需修改 router 中的路由 path**。例如 `/#/dashboard` 会在 nginx 层以 `/admin/` 路径请求，Vite 的 `base: '/admin/'` 确保静态资源路径正确。

由外层的 nginx 转发规则 `location /admin/ { proxy_pass http://frontend_upstream/; }` 负责将 `/admin/` 前缀剥离，后台 nginx 容器收到的原始路径为 `/`。

**不动的部分：**
- 所有内部路由（dashboard、system/admin、system/role 等）不变
- 所有 API 调用路径不变（`/api/v1/...`）
- 登录逻辑不变（仍调 `/system/umsAdmin/login`）

### 3.3 用户前台 SPA（front-user）base 路径迁移

| 文件 | 改动 |
|------|------|
| `front-user/vite.config.ts` | 加 `base: '/user/'` |
| `front-user/vite.config.ts` | proxy target 保持 `localhost:8081` |

同样使用 hash 路由，无需修改 router 的 path。

### 3.4 Docker 部署

docker-compose 中现有容器不变，新增 `front-landing` 容器：

```yaml
front-landing:
  image: ghcr.io/renjianfeng8/wealth-service-platform/wealth-front-landing:latest
  container_name: wealth-front-landing
  mem_limit: 32m
  ports:
    - "3003:80"
  networks:
    - wealth-net
```

nginx upstream 新增：
```nginx
upstream landing_upstream {
    server front-landing:80 max_fails=3 fail_timeout=10s;
    keepalive 16;
}
```

---

## 4. Nginx 配置变更

```nginx
# === 入口：thin SPA（公开首页 + 统一登录）===
location / {
    proxy_pass http://landing_upstream;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

# === 管理后台 SPA ===
location /admin/ {
    proxy_pass http://frontend_upstream/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    # ... 其余 proxy 头同上
}

# === 用户前台 SPA ===
location /user/ {
    proxy_pass http://frontuser_upstream/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    # ... 其余 proxy 头同上
}

# === API ===（不变）
location ~ ^/api/v1/... {
    rewrite ^/api/v1(/.*) $1 break;
    proxy_pass http://gateway_upstream;
    # ...
}
```

---

## 5. 开发环境适配

本地开发时，两个 SPA 的 Vite proxy 需要配置 base 路径：

```ts
// front/vite.config.ts
export default defineConfig({
  base: '/admin/',
  server: {
    port: 3000,
    proxy: { '/api/v1': { target: 'http://localhost:8081', rewrite: path => path.replace(/^\/api\/v1/, '') } }
  }
})

// front-user/vite.config.ts
export default defineConfig({
  base: '/user/',
  server: {
    port: 3001,
    proxy: { '/api/v1': { target: 'http://localhost:8081', rewrite: path => path.replace(/^\/api\/v1/, '') } }
  }
})

// front-landing/vite.config.ts
export default defineConfig({
  base: '/',
  server: {
    port: 3002,  // 本地用 3002 开发
    proxy: { '/api/v1': { target: 'http://localhost:8081', rewrite: path => path.replace(/^\/api\/v1/, '') } }
  }
})
```

本地访问方式：
- `http://localhost:3002` → thin SPA（开发）
- `http://localhost:3000/admin/` → 开发管理后台（base=/admin/，`/#/dashboard`）
- `http://localhost:3001/user/` → 开发用户前台（base=/user/，`/#/dashboard`）

---

## 6. 不变的内容

以下所有内容**不做改动**：

- 两个现有 SPA 的内部业务代码（路由里的 component、API 调用、store、组件）
- `wealth-service` 的现有业务接口
- Gateway 的路由规则和 JWT 过滤器
- `AuthConstant.PERMIT_ALL_URLS` 保持不变
- `LoginInterceptor` 不变
- `docker-compose.yml` 中其他容器（mysql、redis、prometheus、grafana）不变
- MySQL 表结构不变

---

## 7. 安全注意事项

1. **Token 共享：** 两个用户体系共用 `JWT_SECRET`。管理员 token 和普通用户 token 在 JWT 的 `userType` 字段区分
3. **整页跳转：** 登录后使用 `window.location.href` 整页跳转，避免 SPA 路由冲突
