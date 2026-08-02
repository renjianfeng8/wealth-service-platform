# 批次 1 · 安全与认证完善（验证码 + 登出 + token 续期）— 设计文档

> 日期：2026-08-02
> 关联：后端接口冗余审计（2026-08-02，30 个闲置接口清单）；用户已确认采用「微调后端」方案。
> 关联规范：`docs/CODE-STANDARDS.md`、`CLAUDE.md`（十一、代码扫描清单）

---

## 一、背景与目标

后端接口冗余审计发现约 30 个无前端调用的闲置接口。本批次选取其中「**安全与认证完善**」一类，
通过**前端补全界面/交互/调用逻辑 + 后端最小配套改动**，使 3 个闲置认证接口真正闭环：

| 闲置接口 | 前端补全内容 |
|---|---|
| `GET /system/captcha` | 验证码接入登录页 / 注册页（图片 + 输入 + 点击刷新 + 提交校验） |
| `POST /system/umsAdmin/refresh` | 静默 token 续期（401 时自动换取新 token 并重放原请求） |
| `POST /system/umsAdmin/logout` | 登出时调用后端将 refresh_token 加入黑名单 |

配套（已在使用、需最小改动以支撑上述闭环）：
- `POST /user/identify-login` — 前端实际登录路径，需补验证码校验 + 签发 refresh_token
- `POST /user/register` — 需补验证码校验

**范围确认（用户已认可）：** 仅本批 5 个接口；其余 27 个闲置接口不在本次范围。

---

## 二、现状与关键约束（核查结论）

### 2.1 阻断原因
前端统一登录走 `identifyLogin`（`UserServiceImpl.identifyLogin`），该路径：
- **不校验验证码**（验证码校验机制只挂在 `/system/umsAdmin/login` → `UmsAdminAuthServiceImpl.login`）；
- **只签发单个 access token**（`LoginVO` 无 refresh_token 字段），返回 `LoginVO{token,userId,nickname,userType,expiresInSeconds}`。

而 `refresh` / `logout` 接口均要求 `Authorization: Bearer <refresh_token>`。前端当前**没有 refresh_token
来源**，因此三项在「后端完全不动」前提下无法闭环 → 用户批准「微调后端」（最小改动）。

### 2.2 已验证的安全结论（设计前提）
1. **`userType` claim 后端认证不读取**：`getUserTypeFromToken` 仅测试用到；网关 `JwtAuthGlobalFilter`
   与 `LoginInterceptor` 只验签名 + 有效期。因此 identifyLogin 签发 refresh_token 后，续期产生的
   无 userType 新 access token 不会破坏鉴权；前端 admin/user 判定来自 `LoginVO.userType` 响应体，与 claim 无关。
2. **`/system/umsAdmin/refresh` 已在网关白名单**（`AuthConstant.PERMIT_ALL_URLS`），可在 access token
   过期时无凭据调用。
3. **refresh_token 本身是有效 JWT**，可作 `Authorization: Bearer` 通过网关与 `LoginInterceptor` 的
   有效性校验 → `logout` 接口（不在白名单）用 refresh_token 作 Bearer 也能到达服务层完成黑名单。
4. **验证码为「可选」校验**（提供 captchaKey 才 verify，见 `UmsAdminAuthServiceImpl.login`），
   向后兼容，不破坏旧客户端 / 旧前端。

### 2.3 其余不变
- `init.sql` / `application.yml` / 网关路由 / 前端路由 均无改动。
- 其余 27 个闲置接口不动。

---

## 三、方案设计

### 3.1 后端最小改动（6 处）

| # | 文件 | 改动 |
|---|---|---|
| 1 | `user/vo/LoginVO.java` | 增加 `refreshToken` 字段（`@AllArgsConstructor` 构造随之扩展，3 处 `new LoginVO(...)` 需同步） |
| 2 | `common/constants/AuthConstant.java` | 增加 `public static final String REFRESH_JTI_KEY_PREFIX = "refresh:jti:";` |
| 3 | `system/service/impl/UmsAdminAuthServiceImpl.java` | `KEY_REFRESH_JTI` 由字面量 `"refresh:jti:"` 改为引用 `AuthConstant.REFRESH_JTI_KEY_PREFIX` |
| 4 | `user/service/impl/UserServiceImpl.java` | 注入 `CaptchaService`、`RedisUtil`；`identifyLogin` 前置可选验证码校验 + 签发 access/refresh 双 token + 持久化 refresh jti；`register` 前置可选验证码校验 |
| 5 | `user/dto/UserDTO.java` | 增加 `captchaKey`、`captchaCode` 字段 |
| 6 | `system/controller/UmsAdminController.java` | `refresh` 返回类型改 `ResponseEntity<Result<TokenPair>>`，追加 `Set-Cookie` 写新 access token |

**3.1.4 `UserServiceImpl` 改动细节：**

```java
// 新增注入
private final CaptchaService captchaService;
private final RedisUtil redisUtil;

// identifyLogin 开头（assertCredentialsPresent 之后，镜像 admin 登录顺序）
if (StringUtils.hasText(dto.getCaptchaKey())) {
    captchaService.verify(dto.getCaptchaKey(), dto.getCaptchaCode());
}

// 管理员分支（原第 6-7 步之间替换 token 签发）
TokenPair pair = issueUserTokenPair(admin.getUsername(), "admin");
return new LoginVO(pair.accessToken(), admin.getId(), admin.getNickname(), "admin",
        jwtUtil.getAccessExpire() / 1000, pair.refreshToken());

// 普通用户分支（原第 8-9 步之间替换 token 签发）
TokenPair pair = issueUserTokenPair(user.getUsername(), "user");
return new LoginVO(pair.accessToken(), user.getId(), user.getNickname(), "user",
        jwtUtil.getAccessExpire() / 1000, pair.refreshToken());

// 私有辅助：access 保留 userType claim，refresh 走标准长时效签发，并持久化 jti 供现有 refresh 接口复用
private TokenPair issueUserTokenPair(String username, String userType) {
    String accessToken = jwtUtil.generateToken(username, userType);
    String refreshToken = jwtUtil.generateRefreshToken(username);
    String jti = jwtUtil.getTokenIdFromToken(refreshToken);
    redisUtil.safeExecuteVoid(() -> redisUtil.set(
            AuthConstant.REFRESH_JTI_KEY_PREFIX + jti, username, 7, TimeUnit.DAYS),
            "refresh_token 未持久化");
    return new TokenPair(accessToken, refreshToken, jwtUtil.getAccessExpire());
}

// register 开头
if (StringUtils.hasText(dto.getCaptchaKey())) {
    captchaService.verify(dto.getCaptchaKey(), dto.getCaptchaCode());
}
```

> 跨域说明：`UserServiceImpl` 注入 system 域 `CaptchaService`，属同进程单体可接受的跨域依赖；
> 若后续要严格解耦，可抽 Contract 接口，本批次不做。

**3.1.6 `UmsAdminController.refresh` 改动细节：**

```java
@PostMapping("/refresh")
@Operation(summary = "刷新 Token（用 refresh_token 换取新的 access_token + refresh_token）")
public ResponseEntity<Result<TokenPair>> refresh(@RequestHeader("Authorization") String authHeader) {
    TokenPair pair = umsAdminAuthService.refreshToken(authHeader);
    ResponseCookie cookie = CookieUtil.buildTokenCookie(pair.accessToken(), pair.expiresIn() / 1000);
    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(Result.success(pair));
}
```

响应 JSON body 与原 `Result<TokenPair>` 一致，仅新增 Set-Cookie，向后兼容。

### 3.2 前端改动

#### 3.2.1 API 层
- `api/system.ts` 新增：
  ```ts
  export function getCaptcha() {
    return request.get<{ captchaKey: string; captchaImage: string }>('/system/captcha')
  }
  // 登出为 fire-and-forget：用裸 axios 绕过 request 拦截器，避免 refresh token 失效时
  // 401 → 拦截器再次登出/跳转的递归循环；调用方无需关心结果
  export function logoutApi(refreshToken: string) {
    return axios.post('/api/v1/system/umsAdmin/logout', null, {
      headers: { Authorization: `Bearer ${refreshToken}` },
    }).catch(() => undefined)
  }
  ```
  （`refresh` 的调用不在 API 层，见 3.2.4 拦截器内 `tryRefresh` 的裸 axios 实现，避免重复路径。）
  `api/system.ts` 需补充 `import axios from 'axios'`。
- `api/user.ts` 参数类型扩展（additive）：
  ```ts
  export function registerUser(data: { username: string; password: string; captchaKey?: string; captchaCode?: string })
  export function identifyLogin(data: { username: string; password: string; captchaKey?: string; captchaCode?: string })
  ```

#### 3.2.2 `utils/auth.ts` — refresh_token 存储与时间戳 bump
```ts
const REFRESH_KEY = 'wealth_refresh'
export function setRefreshToken(token: string) { sessionStorage.setItem(REFRESH_KEY, token) }
export function getRefreshToken() { return sessionStorage.getItem(REFRESH_KEY) }
export function removeRefreshToken() { sessionStorage.removeItem(REFRESH_KEY) }
export function bumpLoginTime() { sessionStorage.setItem(LOGIN_TIME_KEY, String(Date.now())) }
```
- `removeToken()` 追加调用 `removeRefreshToken()`。

#### 3.2.3 `store/index.ts`
- `state` 增加 `refreshToken: getRefreshToken() || ''`；
- `LoginInfo` 接口增加 `refreshToken?: string`；
- `setLoginInfo(info)` 内持久化 refreshToken；
- `logout()`：先 fire-and-forget 调 `logoutApi(this.refreshToken)`（**用裸 axios，绕过 request 拦截器**，
  避免 401 失败时递归触发登出/跳转），再执行现有清空逻辑；
- 新增 `forceLogout()`：仅清空本地状态，不调后端（供拦截器续期失败路径复用，避免循环）；
- 新增 `applyRefreshedPair(pair)`：更新存储 refreshToken + `bumpLoginTime()`（access token 由后端
  Set-Cookie 写入 httpOnly cookie，前端无需存储）。

#### 3.2.4 `api/index.ts` — 静默续期拦截器
```ts
const PUBLIC_AUTH_PATHS = ['/user/login', '/user/register', '/user/identify-login', '/system/captcha', '/system/umsAdmin/login', '/system/umsAdmin/refresh', '/system/umsAdmin/logout']
let refreshing: Promise<boolean> | null = null

async function tryRefresh(): Promise<boolean> {
  const refresh = getRefreshToken()
  if (!refresh) return false
  if (!refreshing) {
    // 用裸 axios：不经过本拦截器，避免续期请求自身的 401 触发递归
    refreshing = axios.post('/api/v1/system/umsAdmin/refresh', null,
      { headers: { Authorization: `Bearer ${refresh}` } })
      .then(({ data }) => { useUserStore().applyRefreshedPair(data.data); return true })
      .catch(() => false)
      .finally(() => { refreshing = null })
  }
  return refreshing
}
```
- **成功分支** `res.code === 401`：若请求非 PUBLIC_AUTH_PATHS 且存在 refresh_token →
  `await tryRefresh()` 成功后 `return request(response.config)` 重放原请求；失败 → `forceLogout()` + 跳登录。
  公开路径（登录/注册/验证码）→ 仅 `Promise.reject(res.message)`，不登出不跳转。
- **错误分支** `status === 401`：同上逻辑（非公开路径 → tryRefresh 重放或 forceLogout 跳转）。
- 并发 401 去重：`refreshing` 单例 Promise，多个失败请求共享一次续期。

#### 3.2.5 新增 `components/CaptchaField.vue`（可复用）
- `v-model`：验证码输入值；`defineExpose`：`{ reload, getCaptchaKey }`（父组件提交时用 `getCaptchaKey()` 读取 key）；
- 内部：`getCaptcha()` 拉取 `{captchaKey, captchaImage}`，`<img>` 展示，点击图片触发 `reload()`；
- `reload()` 在 `onMounted` 与失败重取时调用；加载中显示占位；拉取失败时清空 key/图片（后端降级跳过校验，不阻塞登录）。

#### 3.2.6 `views/auth/login/index.vue`
- 表单增加验证码字段（`CaptchaField`），`handleLogin` 提交携带 `captchaKey`/`captchaCode`；
- 成功：`const { userId, nickname, userType, refreshToken } = res.data || {}`，
  `userStore.setLoginInfo({ username, userId, nickname, role: userType==='admin'?'admin':'user', refreshToken })`；
- 失败（catch）：`captchaRef.value?.reload()` 刷新验证码。

#### 3.2.7 `views/register/index.vue`
- 同登录页接入 `CaptchaField`，`handleRegister` 提交携带 `captchaKey`/`captchaCode`；
- 注册失败刷新验证码。

### 3.3 关键设计决策

| 决策 | 结论 | 理由 |
|---|---|---|
| refresh_token 存储 | sessionStorage | 与现有登录标记一致；httpOnly refresh cookie 需后端再写 cookie，超最小改动。代价：XSS 可读 refresh，可接受折中 |
| 续期触发 | 401 被动静默续期 | 简单可靠；主动定时续期需读取 access 到期时刻（httpOnly 不可读），复杂度高收益低 |
| 登出 API 调用方式 | 裸 axios fire-and-forget | 避免经 request 拦截器递归触发 401 → 登出循环 |
| 验证码必填性 | 可选（后端 if 提供才校验） | 向后兼容旧客户端，不改变现有登录行为 |

---

## 四、测试计划

### 后端（JUnit 5 + Mockito，方法命名 `{方法名}_should_{预期}`）
- `UserServiceImplTest`：
  - `identifyLogin_should_verify_captcha_when_key_provided`（提供 captchaKey → 调 verify）
  - `identifyLogin_should_skip_captcha_when_key_blank`（不提供 → 不调 verify）
  - `identifyLogin_should_return_refresh_token_and_persist_jti`（返回 refreshToken + Redis 写入）
  - `register_should_verify_captcha_when_provided`
- `UmsAdminControllerTest`（MockMvc）：
  - `refresh_should_return_new_token_pair_and_set_cookie`（Set-Cookie 头含新 access token）
- 回归：现有登录/注册测试不破坏（构造 `LoginVO` 位置同步）。

### 前端
- `CaptchaField`：挂载拉取图片、点击刷新、加载态。
- 登录页：提交携带 captcha 字段；验证码错误 → 刷新验证码。
- 拦截器：401 静默续期 + 重放原请求；续期失败 → 登出跳转；公开接口 401 不触发续期。
- E2E（Playwright，`front/tests/`）：登录页验证码流程冒烟（可选本批，可手动验证替代）。

---

## 五、约束与禁止

- 不改动其余 27 个闲置接口。
- 不修改 `init.sql`、`application.yml`、网关路由、前端路由。
- 验证码保持可选校验，不破坏旧客户端。
- 异常/提示信息统一中文。
- 无通配符 import；魔法值抽常量。
- 后端 `LoginVO` 构造变更涉及 3 处调用，须一并修改。
- git 提交/推送须用户明确指令，本批次不自动提交。

---

## 六、验证清单

- [ ] `mvn clean install -pl wealth-common -DskipTests` 通过
- [ ] `mvn test -pl wealth-service -DskipTests=false` 全量通过
- [ ] 前端 `npm run build`（类型检查 + 构建）通过
- [ ] grep 确认 `identifyLogin`/`register` 已接验证码校验、refresh 接口已 Set-Cookie
- [ ] 手动验证：登录提交验证码成功 / 验证码错误提示并刷新 / 登出后刷新 token 无法再续期 /
      access token 过期（改短 access-expire 或等待 30 分钟）后自动静默续期不跳登录
