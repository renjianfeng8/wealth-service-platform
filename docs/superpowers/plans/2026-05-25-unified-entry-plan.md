# 单一域名统一入口 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 `rjfwealth.cn` 单域名统一入口，根据用户角色（admin/user）自动路由到对应 SPA

**Architecture:** 新增 thin SPA（front-landing）作为默认入口 + 后端统一登录接口识别角色 + nginx 路径分发。两个现有 SPA（front、front-user）只改 base 路径，内部代码不动。后端新增 `identify-login` 端点，双表查询判断用户类型。

**Tech Stack:** Spring Boot 3.3.13 + JWT (jjwt 0.12.6) + Vue 3.5 + Vite 6 + Element Plus + Nginx

---

### Task 1: 后端 — JwtUtil 增加 userType 支持

**Files:**
- Modify: `wealth-common/src/main/java/com/wealth/common/utils/JwtUtil.java`

- [ ] **Step 1: 修改 JwtUtil 增加 userType 支持**

当前 `generateToken(username)` 不支持 userType。添加重载方法使其支持：

```java
/** 生成带 userType 的 Token */
public String generateToken(String username, String userType) {
    return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(username)
            .claim("userType", userType)  // "admin" | "user"
            .expiration(new Date(System.currentTimeMillis() + accessExpire))
            .signWith(getSigningKey())
            .compact();
}

/** 从 Token 获取 userType */
public String getUserTypeFromToken(String token) {
    Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return claims.get("userType", String.class);
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -pl wealth-common -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add wealth-common/src/main/java/com/wealth/common/utils/JwtUtil.java
git commit -m "feat(common): JwtUtil 增加 userType 支持"
```

---

### Task 2: 后端 — 统一登录接口 identifyLogin

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/service/UserService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/service/impl/UserServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/controller/UserController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/vo/LoginVO.java`

- [ ] **Step 1: LoginVO 增加 userType 字段**

```java
// LoginVO.java - 增加 userType 字段
@Data
@AllArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {
    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户类型 admin/user")
    private String userType;
}
```

注意：现有 `UserServiceImpl.login()` 构造 `new LoginVO(jwtUtil.generateToken(...), ...)` 现在少参数，需要同步更新。

- [ ] **Step 2: UserService 接口增加 identifyLogin**

```java
// UserService.java
LoginVO identifyLogin(LoginDTO dto);
```

- [ ] **Step 3: UserServiceImpl 实现 identifyLogin**

```java
@Override
public LoginVO identifyLogin(LoginDTO dto) {
    if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
        throw new ServiceException(400, "用户名/密码不能为空");
    }

    // 1. 先查 ums_admin 表 — 判断是否为管理员
    UmsAdmin admin = umsAdminService.lambdaQuery()
            .eq(UmsAdmin::getUsername, dto.getUsername())
            .one();

    if (admin != null) {
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new ServiceException(401, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new ServiceException(401, "密码错误");
        }
        String token = jwtUtil.generateToken(admin.getUsername(), "admin");
        return new LoginVO(token, admin.getId(), admin.getNickName(), "admin");
    }

    // 2. 再查 user 表 — 判断是否为普通用户
    User user = this.lambdaQuery()
            .eq(User::getUsername, dto.getUsername())
            .one();

    if (user != null) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException(401, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ServiceException(401, "密码错误");
        }
        String token = jwtUtil.generateToken(user.getUsername(), "user");
        return new LoginVO(token, user.getId(), user.getNickname(), "user");
    }

    // 3. 都没找到
    throw new ServiceException(401, "用户名或密码错误");
}
```

需要注入 `UmsAdminService` 和 `BCryptPasswordEncoder`：

```java
// UserServiceImpl 新增字段
private final UmsAdminService umsAdminService;

// 构造函数注入
public UserServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder,
                       UmsAdminService umsAdminService) {
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
    this.umsAdminService = umsAdminService;
}
```

- [ ] **Step 4: UserController 新增 identify-login 端点**

```java
@PostMapping("/identify-login")
@Operation(summary = "统一登录（自动识别用户类型）")
@AuditLog(module = "用户管理", operation = "统一登录")
public Result<LoginVO> identifyLogin(@Valid @RequestBody LoginDTO dto) {
    return Result.success(userService.identifyLogin(dto));
}
```

- [ ] **Step 5: 修复现有的 UserServiceImpl.login() 构造**

```java
// 原来: return new LoginVO(token, dbUser.getId(), dbUser.getNickname());
// 改为:
return new LoginVO(token, dbUser.getId(), dbUser.getNickname(), "user");
```

- [ ] **Step 6: 编译验证**

```bash
mvn compile -pl wealth-service -q
```

Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add wealth-service/src/main/java/com/wealth/platform/user/service/UserService.java \
      wealth-service/src/main/java/com/wealth/platform/user/service/impl/UserServiceImpl.java \
      wealth-service/src/main/java/com/wealth/platform/user/controller/UserController.java \
      wealth-service/src/main/java/com/wealth/platform/user/vo/LoginVO.java
git commit -m "feat(service): 新增 identify-login 统一登录接口"
```

---

### Task 3: 后端 — identifyLogin 单元测试

**Files:**
- Create: `wealth-service/src/test/java/com/wealth/platform/user/service/impl/IdentifyLoginTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentifyLoginTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UmsAdminService umsAdminService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(jwtUtil, passwordEncoder, umsAdminService);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("统一登录-管理员成功")
    void identifyLogin_AdminSuccess() {
        UmsAdmin admin = new UmsAdmin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encodedAdminPwd");
        admin.setStatus(1);
        admin.setNickName("管理员");

        when(umsAdminService.lambdaQuery()).thenAnswer(invocation -> {
            LambdaQueryChainWrapper<UmsAdmin> wrapper = mock(LambdaQueryChainWrapper.class);
            when(wrapper.eq(any(), any())).thenReturn(wrapper);
            when(wrapper.one()).thenReturn(admin);
            return wrapper;
        });
        when(passwordEncoder.matches("admin123", "encodedAdminPwd")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "admin")).thenReturn("admin.jwt.token");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginVO result = userService.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("admin", result.getUserType());
        assertEquals("admin.jwt.token", result.getToken());
        assertEquals(1L, result.getUserId());
        verify(umsAdminService).lambdaQuery();
    }

    @Test
    @DisplayName("统一登录-普通用户成功")
    void identifyLogin_UserSuccess() {
        when(umsAdminService.lambdaQuery()).thenAnswer(invocation -> {
            LambdaQueryChainWrapper<UmsAdmin> wrapper = mock(LambdaQueryChainWrapper.class);
            when(wrapper.eq(any(), any())).thenReturn(wrapper);
            when(wrapper.one()).thenReturn(null);
            return wrapper;
        });

        User user = new User();
        user.setId(2L);
        user.setUsername("testuser");
        user.setPassword("encodedUserPwd");
        user.setStatus(1);
        user.setNickname("测试用户");

        when(userMapper.selectList(any())).thenReturn(List.of());
        // Mock lambdaQuery for user
        LambdaQueryChainWrapper<User> userWrapper = mock(LambdaQueryChainWrapper.class);
        when(userWrapper.eq(any(), any())).thenReturn(userWrapper);
        when(userWrapper.one()).thenReturn(user);

        // Use spy to mock lambdaQuery
        UserServiceImpl spy = spy(userService);
        doReturn(userWrapper).when(spy).lambdaQuery();

        when(passwordEncoder.matches("user123", "encodedUserPwd")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "user")).thenReturn("user.jwt.token");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("user123");

        LoginVO result = spy.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("user", result.getUserType());
        assertEquals("user.jwt.token", result.getToken());
        assertEquals(2L, result.getUserId());
    }

    @Test
    @DisplayName("统一登录-账号不存在抛异常")
    void identifyLogin_NotFound() {
        when(umsAdminService.lambdaQuery()).thenAnswer(invocation -> {
            LambdaQueryChainWrapper<UmsAdmin> wrapper = mock(LambdaQueryChainWrapper.class);
            when(wrapper.eq(any(), any())).thenReturn(wrapper);
            when(wrapper.one()).thenReturn(null);
            return wrapper;
        });

        LambdaQueryChainWrapper<User> userWrapper = mock(LambdaQueryChainWrapper.class);
        when(userWrapper.eq(any(), any())).thenReturn(userWrapper);
        when(userWrapper.one()).thenReturn(null);

        UserServiceImpl spy = spy(userService);
        doReturn(userWrapper).when(spy).lambdaQuery();

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nobody");
        dto.setPassword("pwd");

        assertThrows(ServiceException.class, () -> spy.identifyLogin(dto));
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
mvn test -pl wealth-service -Dtest=IdentifyLoginTest -DskipTests=false
```

Expected: 3/3 tests PASS

- [ ] **Step 3: 提交**

```bash
git add wealth-service/src/test/java/com/wealth/platform/user/service/impl/IdentifyLoginTest.java
git commit -m "test(service): 添加 identifyLogin 单元测试"
```

---

### Task 4: 前端 — 管理后台 base 路径迁移

**Files:**
- Modify: `front/vite.config.ts`
- Modify: `front/Dockerfile`

- [ ] **Step 1: vite.config.ts 加 base 配置**

```ts
// front/vite.config.ts
export default defineConfig({
  base: '/admin/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, ''),
      },
    },
  },
})
```

- [ ] **Step 2: 提交**

```bash
git add front/vite.config.ts
git commit -m "feat(front): 管理后台 base 路径改为 /admin/"
```

---

### Task 5: 前端 — 用户前台 base 路径迁移

**Files:**
- Modify: `front-user/vite.config.ts`

- [ ] **Step 1: vite.config.ts 加 base 配置**

```ts
// front-user/vite.config.ts
export default defineConfig({
  base: '/user/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3001,
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, ''),
      },
    },
  },
})
```

- [ ] **Step 2: 提交**

```bash
git add front-user/vite.config.ts
git commit -m "feat(front-user): 用户前台 base 路径改为 /user/"
```

---

### Task 6: 前端 — 新建 front-landing 项目脚手架

**Files:**
- Create: `front-landing/package.json`
- Create: `front-landing/index.html`
- Create: `front-landing/vite.config.ts`
- Create: `front-landing/tsconfig.json`
- Create: `front-landing/tsconfig.node.json`
- Create: `front-landing/src/main.ts`
- Create: `front-landing/src/App.vue`
- Create: `front-landing/src/env.d.ts`
- Create: `front-landing/src/router/index.ts`
- Create: `front-landing/src/api/index.ts`
- Create: `front-landing/src/api/user.ts`
- Create: `front-landing/src/utils/auth.ts`
- Create: `front-landing/Dockerfile`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "wealth-front-landing",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc -b && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.5.13",
    "vue-router": "^4.5.0",
    "element-plus": "^2.9.7",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.7.0",
    "pinia": "^2.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.0",
    "typescript": "~5.7.0",
    "vite": "^6.3.1",
    "vue-tsc": "^2.2.0"
  }
}
```

- [ ] **Step 2: 创建 vite.config.ts**

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  base: '/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3002,
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, ''),
      },
    },
  },
})
```

- [ ] **Step 3: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>理财服务平台</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
```

- [ ] **Step 4: 创建 tsconfig.json / tsconfig.node.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true,
    "paths": { "@/*": ["./src/*"] }
  },
  "include": ["src/**/*.ts", "src/**/*.vue", "src/env.d.ts"]
}
```

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 5: 创建 src/env.d.ts**

```ts
/// <reference types="vite/client" />
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
```

- [ ] **Step 6: 创建 src/main.ts**

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus)
app.use(router)
app.mount('#app')
```

- [ ] **Step 7: 创建 src/App.vue**

```vue
<template>
  <router-view />
</template>
```

- [ ] **Step 8: 创建 src/router/index.ts**

```ts
import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
```

- [ ] **Step 9: 创建 src/api/index.ts**

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  if (config.method && ['post', 'put', 'delete'].includes(config.method)) {
    config.headers['X-Timestamp'] = Date.now().toString()
    config.headers['X-Nonce'] = crypto.randomUUID()
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default request
```

- [ ] **Step 10: 创建 src/api/user.ts**

```ts
import request from './index'

export function identifyLogin(data: { username: string; password: string }) {
  return request.post('/user/identify-login', data)
}
```

- [ ] **Step 11: 创建 src/utils/auth.ts**

```ts
const TOKEN_KEY = 'wealth_token'
const USER_KEY = 'wealth_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setStoredUser(user: { username: string; userType: string; nickname?: string }) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getStoredUser(): { username: string; userType: string; nickname?: string } | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}
```

- [ ] **Step 12: 创建 Dockerfile**

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

- [ ] **Step 13: 安装依赖**

```bash
cd front-landing && npm install
```

- [ ] **Step 14: 提交**

```bash
git add front-landing/
git commit -m "feat(landing): 新建 front-landing thin SPA 项目脚手架"
```

---

### Task 7: 前端 — front-landing 首页（公开内容）

**Files:**
- Create: `front-landing/src/views/home/index.vue`

- [ ] **Step 1: 创建首页组件**

展示：
1. 顶部导航栏（logo + 登录按钮 + 已登录则显示用户名）
2. 产品列表卡片
3. 实时行情表格
4. 财经资讯列表

```vue
<template>
  <div class="landing-page">
    <!-- Navbar -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="nav-left">
          <span class="logo">理财服务平台</span>
        </div>
        <div class="nav-right">
          <template v-if="userStore.token">
            <span class="user-info">{{ userStore.nickname || userStore.username }}</span>
            <el-button text @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="hero">
      <h1>智慧投资 · 稳健增值</h1>
      <p>专业理财服务平台，为您提供全方位的投资解决方案</p>
    </section>

    <!-- Product List -->
    <section class="section">
      <h2 class="section-title">产品中心</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="p in products" :key="p.id" class="product-card">
          <el-card shadow="hover">
            <h3>{{ p.productName }}</h3>
            <p class="price">¥{{ p.price }}</p>
            <p class="code">{{ p.productCode }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- Market Data -->
    <section class="section market-section">
      <h2 class="section-title">实时行情</h2>
      <el-table :data="marketData" stripe style="width: 100%">
        <el-table-column prop="productCode" label="产品代码" width="120" />
        <el-table-column prop="currentPrice" label="当前价" width="120" />
        <el-table-column label="涨跌幅" width="120">
          <template #default="{ row }">
            <span :style="{ color: (row.riseFallRate || 0) >= 0 ? '#f56c6c' : '#67c23a' }">
              {{ row.riseFallRate ?? '-' }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="highestPrice" label="最高" width="100" />
        <el-table-column prop="lowestPrice" label="最低" width="100" />
        <el-table-column prop="marketTime" label="时间" />
      </el-table>
    </section>

    <!-- News -->
    <section class="section">
      <h2 class="section-title">财经资讯</h2>
      <div v-for="n in news" :key="n.id" class="news-item">
        <h3>{{ n.title }}</h3>
        <p class="news-meta">{{ n.source }} · {{ n.createTime }}</p>
        <p class="news-summary">{{ n.summary || n.content?.substring(0, 120) }}</p>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>© 2026 理财服务平台. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/index'

const router = useRouter()
const userStore = useUserStore()

const products = ref<any[]>([])
const marketData = ref<any[]>([])
const news = ref<any[]>([])

async function fetchProducts() {
  const res = await request.get('/product/wea-product/page', { params: { pageNum: 1, pageSize: 4 } })
  products.value = res.data?.records ?? []
}

async function fetchMarketData() {
  const res = await request.get('/product/wea-market-data/list')
  marketData.value = res.data ?? []
}

async function fetchNews() {
  const res = await request.get('/message/news/page', { params: { pageNum: 1, pageSize: 5 } })
  news.value = res.data?.records ?? []
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出')
}

onMounted(() => {
  fetchProducts()
  fetchMarketData()
  fetchNews()
})
</script>

<style scoped>
.landing-page { min-height: 100vh; background: #f5f7fa; }
.navbar { background: #1a365d; color: #fff; padding: 0 40px; position: sticky; top: 0; z-index: 100; }
.nav-inner { display: flex; justify-content: space-between; align-items: center; height: 56px; max-width: 1200px; margin: 0 auto; }
.logo { font-size: 18px; font-weight: 700; letter-spacing: 2px; }
.user-info { margin-right: 12px; font-size: 14px; }
.hero { text-align: center; padding: 60px 20px; background: linear-gradient(135deg, #1a365d, #2d5a8e); color: #fff; }
.hero h1 { font-size: 36px; margin-bottom: 12px; }
.hero p { font-size: 16px; opacity: 0.85; }
.section { max-width: 1200px; margin: 32px auto; padding: 0 20px; }
.section-title { font-size: 22px; margin-bottom: 20px; color: #1a365d; }
.product-card { margin-bottom: 16px; }
.price { font-size: 24px; font-weight: 700; color: #e6a23c; }
.code { font-size: 12px; color: #909399; }
.news-item { padding: 16px 0; border-bottom: 1px solid #ebeef5; }
.news-item h3 { font-size: 16px; margin-bottom: 6px; }
.news-meta { font-size: 12px; color: #909399; margin-bottom: 8px; }
.news-summary { font-size: 14px; color: #606266; line-height: 1.6; }
.footer { text-align: center; padding: 24px; color: #909399; font-size: 12px; }
</style>
```

注意：`/product/wea-market-data/list` 如果不存在则改用 `/product/wea-market-data/page`。

- [ ] **Step 2: 提交**

```bash
git add front-landing/src/views/home/index.vue
git commit -m "feat(landing): 首页展示产品/行情/资讯"
```

---

### Task 8: 前端 — front-landing 统一登录页

**Files:**
- Create: `front-landing/src/views/login/index.vue`
- Create: `front-landing/src/stores/user.ts`

- [ ] **Step 1: 创建 user store**

```ts
// front-landing/src/stores/user.ts
import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userType: getStoredUser()?.userType || '',
    nickname: getStoredUser()?.nickname || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setLoginInfo(info: { token: string; username: string; userType: string; nickname?: string }) {
      setToken(info.token)
      setStoredUser({ username: info.username, userType: info.userType, nickname: info.nickname })
      this.token = info.token
      this.username = info.username
      this.userType = info.userType
      this.nickname = info.nickname || ''
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userType = ''
      this.nickname = ''
      removeToken()
    },
  },
})
```

- [ ] **Step 2: 创建登录页**

```vue
<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">登录</h2>
      <p class="subtitle">理财服务平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { identifyLogin } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await identifyLogin({ username: form.username, password: form.password })
    const { token, userType, nickname } = res.data
    userStore.setLoginInfo({
      token,
      username: form.username,
      userType,
      nickname: nickname || undefined,
    })
    ElMessage.success('登录成功')
    // 整页跳转到对应 SPA
    const redirectUrl = userType === 'admin' ? '/admin/#/' : '/user/#/'
    window.location.href = redirectUrl
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f1b2d, #1a365d 40%, #2d5a8e 100%);
}
.login-card {
  width: 400px;
  padding: 36px 40px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0,0,0,0.25);
}
.title { text-align: center; font-size: 24px; font-weight: 700; margin-bottom: 4px; }
.subtitle { text-align: center; font-size: 13px; color: #909399; margin-bottom: 24px; }
.login-btn { width: 100%; height: 42px; font-size: 15px; letter-spacing: 4px; }
</style>
```

- [ ] **Step 3: 验证构建**

```bash
cd front-landing && npx vite build
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add front-landing/src/views/login/index.vue \
      front-landing/src/stores/user.ts
git commit -m "feat(landing): 统一登录页 + user store"
```

---

### Task 9: 部署 — Nginx + Docker 配置

**Files:**
- Modify: `nginx.conf`
- Modify: `docker-compose.yml`

- [ ] **Step 1: 修改 nginx.conf**

```nginx
# === 入口：thin SPA（公开首页 + 统一登录）===
upstream landing_upstream {
    server front-landing:80 max_fails=3 fail_timeout=10s;
    keepalive 16;
}

location / {
    proxy_pass http://landing_upstream;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
    proxy_connect_timeout 10s;
}

# === 管理后台 SPA ===
location /admin/ {
    proxy_pass http://frontend_upstream/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
    proxy_connect_timeout 10s;
}

# === 用户前台 SPA ===
location /user/ {
    proxy_pass http://frontuser_upstream/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
    proxy_connect_timeout 10s;
}
```

同时删除旧的 `/user-portal/` location 块。

- [ ] **Step 2: docker-compose.yml 新增 front-landing 服务**

```yaml
front-landing:
  image: ghcr.io/renjianfeng8/wealth-service-platform/wealth-front-landing:latest
  container_name: wealth-front-landing
  mem_limit: 32m
  memswap_limit: 48m
  ports:
    - "3003:80"
  healthcheck:
    test: ["CMD", "wget", "-q", "-O", "/dev/null", "http://localhost:80/"]
    interval: 30s
    timeout: 5s
    retries: 3
    start_period: 20s
  restart: unless-stopped
  logging:
    options:
      max-size: "50m"
      max-file: "3"
  networks:
    - wealth-net
```

- [ ] **Step 3: 提交**

```bash
git add nginx.conf docker-compose.yml
git commit -m "feat(deploy): nginx 路由 + docker-compose 新增 front-landing"
```

---

### Task 10: 安装 front-landing 依赖并验证本地运行

- [ ] **Step 1: 安装依赖**

```bash
cd front-landing && npm install
```

- [ ] **Step 2: 验证构建**

```bash
cd front-landing && npx vite build
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 确认后端 identify-login 接口可用**

```bash
# 登录管理员
curl -s --noproxy localhost -X POST http://localhost:8081/user/identify-login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 预期: {"code":200,"data":{"token":"xxx","userType":"admin","userId":1,"nickname":"管理员",...}}

# 登录普通用户
curl -s --noproxy localhost -X POST http://localhost:8081/user/identify-login \
  -H "Content-Type: application/json" \
  -d '{"username":"demouser","password":"123456"}'

# 预期: {"code":200,"data":{"token":"xxx","userType":"user","userId":86,...}}
```

---

### 总改动的文件清单

| 操作 | 文件 | 说明 |
|------|------|------|
| Modify | `wealth-common/.../JwtUtil.java` | +userType support |
| Modify | `wealth-service/.../UserService.java` | +identifyLogin |
| Modify | `wealth-service/.../UserServiceImpl.java` | +identifyLogin impl |
| Modify | `wealth-service/.../UserController.java` | +/identify-login |
| Modify | `wealth-service/.../LoginVO.java` | +userType field |
| Create | `wealth-service/.../IdentifyLoginTest.java` | Tests |
| Modify | `front/vite.config.ts` | base: '/admin/' |
| Modify | `front-user/vite.config.ts` | base: '/user/' |
| Create | `front-landing/` (14 files) | Thin SPA |
| Modify | `nginx.conf` | 路由分发 |
| Modify | `docker-compose.yml` | +front-landing service |
