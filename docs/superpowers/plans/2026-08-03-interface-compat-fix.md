# 前后端接口兼容性修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **提交约束（CLAUDE.md）**：本项目禁止自动 `git commit`/`git push`。本计划各任务末尾的「提交」步骤**默认不执行**，由执行者在完成后汇总变更并等待用户明确指令再提交。

**Goal:** 修复审计报告 JIEKOU.md 中的全部 10 项前后端接口契约不一致（H1/H2/M1/M2/M3/L1~L5）。

**Architecture:** 后端统一日期序列化（JacksonConfig 注册 LocalDateTime 全局格式 + 空串容忍）与分页响应命名（Page 自定义序列化器输出 pageNum/pageSize）；前端 axios 拦截器改为返回 `res.data` 并收口 API 类型，全量调用点随之去掉 `.data`；配套精度、校验、命名、文档类小修。

**Tech Stack:** Java 21 / Spring Boot 3.3 / MyBatis-Plus / Jackson / Vue 3 + TS + axios + Element Plus。

**设计文档：** `docs/superpowers/specs/2026-08-03-interface-compat-fix-design.md`

---

## 分阶段执行概览

> 将 10 个任务拆为 3 个阶段，便于分批评审与推荐。阶段内任务按序执行，每阶段结束做局部验证。

| 阶段 | 覆盖问题 | 任务 | 阶段出口 |
|---|---|---|---|
| **阶段一：高危阻断修复（P0）** | H1、H2（+M3 同根因） | Task 1、2、5 | 后端 `wealth-common` 编译 + 前端 `vue-tsc` 通过；admin 行情/资讯**空日期新增编辑不再 400** |
| **阶段二：契约标准化（P1）** | M1、M2、L4（+L5） | Task 3、4、9 | 分页响应 `pageNum/pageSize`；涨跌幅 `DECIMAL(8,4)`；`id` 字符串化；三方对齐（**需审批 init.sql ALTER**） |
| **阶段三：低危清理与全量收尾（P2）** | L1、L2、L3 | Task 6、7、8、10 | 全量 `mvn clean install -DskipTests=false` + `vue-tsc && vite build` 通过；启动冒烟；汇总变更待提交 |

> **拆分说明**：M3 与 H1 同根因（日期格式约定松散），已并入 Task 1/2，随阶段一落地；L4 是响应序列化契约改动，与 M1 分页契约同属「响应契约标准化」，并入阶段二；L1/L2/L3 为纯清理/文档改动，风险极低，合并到阶段三与全量验证收尾一起完成。

---

## 文件结构总览

| 文件 | 动作 | 负责事项 |
|---|---|---|
| `wealth-common/.../config/JacksonConfig.java` | 修改 | 全局 LocalDateTime 格式 + 空串容忍 + Page 序列化器注册 |
| `wealth-common/.../config/FlexibleLocalDateTimeDeserializer.java` | 新建 | LocalDateTime 反序列化（空串→null、主格式、ISO 兜底） |
| `wealth-common/.../config/PageSerializer.java` | 新建 | `Page` 输出 records/total/pageNum/pageSize/pages |
| `wealth-common/.../config/FlexibleLocalDateTimeDeserializerTest.java` | 新建 | 反序列化单测（H1 根因回归） |
| `wealth-common/src/main/resources/sql/init.sql` | 修改 | `rise_fall_rate` 两处 → DECIMAL(8,4) |
| `wealth-common/src/main/java/com/wealth/common/dto/UserFavoriteDTO.java` | 重命名 | → `UserFavoriteProviderDTO.java`（L1） |
| `wealth-service/.../message/dto/BatchReadDTO.java` | 修改 | `ids` 加 `@NotEmpty`（L2） |
| `wealth-service/.../message/controller/MessageController.java` | 修改 | `@Valid`、删手写判空、SSE 无涉（L2） |
| `wealth-service/.../product/service/MarketDataSimulationService.java` | 修改 | `riseFallRate` setScale(4)（M2） |
| `wealth-service/.../product/dto/MarketDataDTO.java` | 修改 | riseFallRate `@DecimalMin/-1` `@DecimalMax/1`（L5） |
| `wealth-service/.../product/dto/ProductDTO.java` | 修改 | riseFallRate `@DecimalMin/-1` `@DecimalMax/1`（L5） |
| 12 个 VO + `LoginVO.java` | 修改 | `id` 字段 `@JsonSerialize(ToStringSerializer)`（L4） |
| `front/src/api/index.ts` | 修改 | 拦截器返回 `res.data`、导出 `ApiClient` 类型（H2） |
| `front/src/types/index.ts` | 修改 | `PageResult` 改 pageNum/pageSize、id 放宽 `number\|string`（M1/L4） |
| ~25 个 view/component/composable | 修改 | `res.data` → `res`（H2） |
| `front/src/api/*.ts` | 修改 | id 入参放宽 `number\|string`（L4） |
| `front/src/views/admin/market/index.vue`、`admin/news/index.vue` | 修改 | value-format 空格格式（H1/M3）、涨跌幅 label（L5） |
| `front/src/store/marketSSE.ts` | 修改 | SSE 载荷注释（L3） |
| `docs/ARCHITECTURE.md` | 修改 | SSE 例外约定（L3） |

---

## 【阶段一】Task 1: 后端日期全局序列化（H1 + M3）

**Files:**
- Create: `wealth-common/src/main/java/com/wealth/common/config/FlexibleLocalDateTimeDeserializer.java`
- Create: `wealth-common/src/test/java/com/wealth/common/config/FlexibleLocalDateTimeDeserializerTest.java`
- Modify: `wealth-common/src/main/java/com/wealth/common/config/JacksonConfig.java`

- [ ] **Step 1: 写失败测试（空串→null，H1 根因回归）**

`wealth-common/src/test/java/com/wealth/common/config/FlexibleLocalDateTimeDeserializerTest.java`:

```java
package com.wealth.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlexibleLocalDateTimeDeserializerTest {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(FORMAT));
        m.registerModule(module);
        return m;
    }

    @Test
    void deserialize_should_convert_empty_string_to_null() throws Exception {
        assertNull(mapper().readValue("\"\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_parse_main_format() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 15, 4, 5),
                mapper().readValue("\"2026-08-03 15:04:05\"", LocalDateTime.class));
    }

    @Test
    void deserialize_should_fallback_to_iso8601() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 3, 10, 30, 0),
                mapper().readValue("\"2026-08-03T10:30:00\"", LocalDateTime.class));
    }

    @Test
    void serialize_should_use_space_format() throws Exception {
        assertEquals("\"2026-08-03 15:04:05\"",
                mapper().writeValueAsString(LocalDateTime.of(2026, 8, 3, 15, 4, 5)));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-common -Dtest=FlexibleLocalDateTimeDeserializerTest -DskipTests=false`
Expected: FAIL（`FlexibleLocalDateTimeDeserializer` 类不存在，编译失败）

- [ ] **Step 3: 新建反序列化器**

`wealth-common/src/main/java/com/wealth/common/config/FlexibleLocalDateTimeDeserializer.java`:

```java
package com.wealth.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime 反序列化：空串视为 null（兼容表单未填日期），主格式 yyyy-MM-dd HH:mm:ss，
 * 兜底兼容 ISO-8601（带 T）旧调用。
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter MAIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text, MAIN);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(text, ISO);
            } catch (DateTimeParseException e2) {
                return ctxt.handleWeirdStringValue(LocalDateTime.class, text, "日期格式应为 yyyy-MM-dd HH:mm:ss");
            }
        }
    }
}
```

- [ ] **Step 4: 修改 JacksonConfig 注册全局格式**

`wealth-common/src/main/java/com/wealth/common/config/JacksonConfig.java` 整体替换为：

```java
package com.wealth.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置 — LocalDateTime 统一 yyyy-MM-dd HH:mm:ss（空串→null），
 * 分页响应统一 pageNum/pageSize。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeModuleCustomizer() {
        return builder -> {
            builder.modulesToInstall(new JavaTimeModule());
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMAT));
            builder.deserializerByType(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
            builder.serializerByType(Page.class, new PageSerializer());
        };
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl wealth-common -Dtest=FlexibleLocalDateTimeDeserializerTest -DskipTests=false`
Expected: PASS（4 个用例）

- [ ] **Step 6: 全量编译**

Run: `mvn clean install -pl wealth-common -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 7: 汇报变更（不提交，等待用户指令）**

---

## 【阶段一】Task 2: 前端日期录入格式对齐（H1 + M3）

**Files:**
- Modify: `front/src/views/admin/market/index.vue:103`
- Modify: `front/src/views/admin/news/index.vue:79`

- [ ] **Step 1: 行情表单日期格式**

`front/src/views/admin/market/index.vue:103`：
`value-format="YYYY-MM-DDTHH:mm:ss"` → `value-format="YYYY-MM-DD HH:mm:ss"`

- [ ] **Step 2: 资讯表单日期格式**

`front/src/views/admin/news/index.vue:79`：同上替换。

- [ ] **Step 3: 前端类型检查**

Run: `cd front && npx vue-tsc --noEmit`
Expected: 无新增错误（模板改动不影响类型；如有既有错误忽略）

- [ ] **Step 4: 汇报变更**

---

## 【阶段二】Task 3: 分页响应命名统一（M1）

**Files:**
- Create: `wealth-common/src/main/java/com/wealth/common/config/PageSerializer.java`
- Modify: `front/src/types/index.ts`（`PageResult`）

- [x] **Step 1: 新建 Page 序列化器**

`wealth-common/src/main/java/com/wealth/common/config/PageSerializer.java`:

```java
package com.wealth.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 分页响应统一为 records/total/pageNum/pageSize/pages，
 * 与请求入参 pageNum/pageSize 命名对齐（替代 MyBatis-Plus 默认 current/size）。
 */
public class PageSerializer extends JsonSerializer<Page<?>> {

    @Override
    public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("records", page.getRecords());
        gen.writeNumberField("total", page.getTotal());
        gen.writeNumberField("pageNum", page.getCurrent());
        gen.writeNumberField("pageSize", page.getSize());
        gen.writeNumberField("pages", page.getPages());
        gen.writeEndObject();
    }
}
```

> 注：已在 Task 1 的 JacksonConfig 中 `serializerByType(Page.class, ...)` 注册。`BeanConvertUtil.convertPage` 运行时返回 `Page`，可覆盖全部 `/page` 接口。

- [x] **Step 2: 更新前端 PageResult 类型**

`front/src/types/index.ts` 中：

```ts
export interface PageResult<T = any> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}
```

- [x] **Step 3: 后端编译**

Run: `mvn clean install -pl wealth-common -DskipTests`
Expected: BUILD SUCCESS

- [x] **Step 4: 冒烟验证分页响应字段**

以 `PageSerializerTest`（wealth-common 单测）验证 `{records,total,pageNum,pageSize,pages}` 输出、无 `current/size`；真实服务冒烟留待 Task 10。

- [x] **Step 5: 汇报变更**

---

## 【阶段二】Task 4: 涨跌幅精度（M2）+ 单位提示与校验（L5）

**Files:**
- Modify: `wealth-common/src/main/resources/sql/init.sql`
- Modify: `wealth-service/.../product/service/MarketDataSimulationService.java:101-103`
- Modify: `wealth-service/.../product/dto/MarketDataDTO.java`
- Modify: `wealth-service/.../product/dto/ProductDTO.java`
- Modify: `front/src/views/admin/market/index.vue:97`

- [x] **Step 1: 更新 init.sql 两处精度**

`wealth-common/src/main/resources/sql/init.sql`：
- 第 41 行 `wea_product.rise_fall_rate DECIMAL(5,2)` → `rise_fall_rate DECIMAL(8,4)`
- 第 64 行 `wea_market_data.rise_fall_rate DECIMAL(5,2)` → `rise_fall_rate DECIMAL(8,4)`

现有库迁移（已附在 init.sql 末尾注释区）：
```sql
ALTER TABLE wea_product MODIFY COLUMN rise_fall_rate DECIMAL(8,4) DEFAULT NULL COMMENT '涨跌幅';
ALTER TABLE wea_market_data MODIFY COLUMN rise_fall_rate DECIMAL(8,4) DEFAULT NULL COMMENT '涨跌幅';
```
> 用户已批准表结构变更（2026-08-03）。ALTER 已在 init.sql 注释区记录，对现有库的执行由用户决定时机。

- [x] **Step 2: 模拟服务精度对齐**

`MarketDataSimulationService.java:101-103`：`.divide(closePrice, 6, RoundingMode.HALF_UP)` → `.divide(closePrice, 4, RoundingMode.HALF_UP)`

- [x] **Step 3: 两个 DTO 加涨跌幅范围校验**

`MarketDataDTO.java` 的 `riseFallRate` 字段前加：
```java
@DecimalMin(value = "-1", message = "涨跌幅不能低于-100%")
@DecimalMax(value = "1", message = "涨跌幅不能高于100%")
```
`ProductDTO.java` 的 `riseFallRate` 字段前同样加上（确认已 import `jakarta.validation.constraints.DecimalMin/DecimalMax`；缺失则补 import）。

- [x] **Step 4: 前端涨跌幅单位提示**

`front/src/views/admin/market/index.vue:97`：`label="涨跌幅"` → `label="涨跌幅（小数，如 0.05=5%）"`

- [x] **Step 5: 编译**

Run: `mvn clean install -pl wealth-common -DskipTests`，随后 `mvn clean install -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS

- [x] **Step 6: 汇报变更（涉及 init.sql 表结构，已获用户批准；ALTER 对现有库的执行待定）**

---

## 【阶段一】Task 5: H2 拦截器返回 res.data + API 类型收口（前端全量）

**Files:**
- Modify: `front/src/api/index.ts`
- Modify（`res.data` → `res`）：
  - `front/src/components/CaptchaField.vue:43`
  - `front/src/components/ProductDetailDialog.vue:97,111`
  - `front/src/components/NewsDetailDialog.vue:49`
  - `front/src/components/MessageNoticePopover.vue:120-121`
  - `front/src/composables/useAdminDashboard.ts:47,54,63,71,76,82`
  - `front/src/layouts/Navbar.vue:152-153`
  - `front/src/views/dashboard/index.vue:334,375,387`
  - `front/src/views/favorite/index.vue:148,181-182`
  - `front/src/views/home/index.vue:265-266,279,291`
  - `front/src/views/auth/login/index.vue:127`
  - `front/src/views/admin/market/index.vue:184-185`
  - `front/src/views/trade/index.vue:338-339,375`
  - `front/src/views/admin/message/index.vue:122-123`
  - `front/src/views/admin/favorite/index.vue:96-97`
  - `front/src/views/admin/profile/index.vue:286`
  - `front/src/views/news/index.vue:109-110`
  - `front/src/views/admin/news/index.vue:132-133`
  - `front/src/views/admin/search/index.vue:98-99`
  - `front/src/views/market/index.vue:132-133`
  - `front/src/views/admin/user/index.vue:145-146`
  - `front/src/views/admin/product/index.vue:199-200`
  - `front/src/views/admin/trade/index.vue:173-174`
  - `front/src/views/products/index.vue:158-159`
  - `front/src/views/profile/index.vue:281`
  - `front/src/views/market/MarketDetailDialog.vue:77`
  - `front/src/views/message/index.vue:108-109,123,146`
  - `front/src/views/admin/system/admin/index.vue:132-133`
  - `front/src/views/admin/system/adminRole/index.vue:142-143`
  - `front/src/views/admin/system/roleResource/index.vue:142-143`
  - `front/src/views/admin/system/role/index.vue:115-116`
  - `front/src/views/admin/system/resource/index.vue:101-102`

- [ ] **Step 1: 重写 api/index.ts**

`front/src/api/index.ts` 整体替换为（关键变化：成功分支 `return res.data`；导出类型为 `ApiClient`）：

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import router from '@/router'
import { getRefreshToken } from '@/utils/auth'
import { randomUUID } from '@/utils/uuid'
import type { AxiosRequestConfig } from 'axios'

const redirectLogin = () => {
  router.replace('/auth/login')
}

// 公开认证路径：业务 401（如密码错误）只提示，不触发续期/登出
const PUBLIC_AUTH_PATHS = [
  '/user/register',
  '/user/identify-login',
  '/system/captcha',
  '/system/umsAdmin/refresh',
  '/system/umsAdmin/logout',
]

/**
 * 业务方法类型：拦截器已解析 res.data，返回 Promise<T>（而非 AxiosResponse<T>），
 * 与运行期一致。
 */
export interface ApiClient {
  <T = unknown>(config: AxiosRequestConfig): Promise<T>
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
}

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
})

// 并发 401 去重：多个失败请求共享一次续期
let refreshing: Promise<boolean> | null = null

/**
 * 静默续期：用 refresh_token 换取新 token 对（后端 Set-Cookie 写新 access token）。
 * 使用裸 axios 调用，不经过本拦截器，避免续期请求自身的 401 触发递归。
 */
async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  if (!refreshing) {
    refreshing = axios.post('/api/v1/system/umsAdmin/refresh', null, {
      headers: { Authorization: `Bearer ${refreshToken}` },
    })
      .then(({ data }) => {
        useUserStore().applyRefreshedPair(data.data)
        return true
      })
      .catch(() => false)
      .finally(() => { refreshing = null })
  }
  return refreshing
}

function isPublicAuthPath(url?: string): boolean {
  return !!url && PUBLIC_AUTH_PATHS.some((p) => url.includes(p))
}

function failToLogin(message: string) {
  useUserStore().forceLogout()
  redirectLogin()
  ElMessage.error(message || '登录已过期，请重新登录')
}

request.interceptors.request.use((config) => {
  if (config.method && ['post', 'put', 'delete'].includes(config.method)) {
    config.headers['X-Timestamp'] = Date.now().toString()
    config.headers['X-Nonce'] = randomUUID()
  }
  return config
})

request.interceptors.response.use(
  (response): any => {
    const res = response.data
    if (res.code === 401) {
      const config = response.config as { url?: string; _refreshed?: boolean }
      if (!isPublicAuthPath(config.url) && !config._refreshed) {
        const ok = await tryRefresh()
        if (ok) {
          config._refreshed = true
          return request(response.config)
        }
      }
      if (isPublicAuthPath(config.url)) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      failToLogin(res.message || '未登录')
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res.data
  },
  async (error) => {
    const status = error.response?.status
    const config = error.config as { url?: string; _refreshed?: boolean } | undefined
    if (status === 401) {
      if (config && !isPublicAuthPath(config.url) && !config._refreshed) {
        const ok = await tryRefresh()
        if (ok) {
          config._refreshed = true
          return request(config)
        }
      }
      if (config && isPublicAuthPath(config.url)) {
        const data = error.response?.data
        ElMessage.error(data?.message || '请求失败')
      } else {
        failToLogin('登录已过期，请重新登录')
      }
    } else {
      const data = error.response?.data
      const msg = data?.message || error.message || '网络错误'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request as unknown as ApiClient
```

> 说明：`tryRefresh`/`logoutApi` 使用裸 `axios`，返回 `AxiosResponse`，不受影响。401 续期重放 `request(response.config)` 的返回值即 `res.data`，语义一致。

- [ ] **Step 2: 全量替换 res.data → res**

对上述列出的每个文件，执行规则化替换（优先用编辑器全局替换，逐个核对）：
- `res.data?.records` → `res?.records`
- `res.data.records` → `res.records`
- `res.data?.total` → `res?.total`
- `res.data.total` → `res.total`
- `res.data?.candles` → `res?.candles`
- `res.data || null` → `res || null`
- `res.data || {}` → `res || {}`
- `res.data ?? item` → `res ?? item`
- `const data = res.data` → `const data = res`
- `if (res.data) trendData.value = res.data` → `if (res) trendData.value = res`
- `overview.value = res.data` → `overview.value = res`

- [ ] **Step 3: 类型检查兜底**

Run: `cd front && npx vue-tsc --noEmit`
Expected: 0 错误（如有残留 `res.data` 编译报错，按报错位置修复）

- [ ] **Step 4: 构建**

Run: `cd front && npx vite build`
Expected: 构建成功

- [ ] **Step 5: 汇报变更**

---

## 【阶段三】Task 6: L1 同名 DTO 重命名

**Files:**
- Rename: `wealth-common/src/main/java/com/wealth/common/dto/UserFavoriteDTO.java` → `UserFavoriteProviderDTO.java`

- [x] **Step 1: 重命名文件与类**

类名 `UserFavoriteDTO` → `UserFavoriteProviderDTO`，`@Schema(description = "用户自选DTO")` → `@Schema(description = "跨服务用户自选DTO")`。包路径不变 `com.wealth.common.dto`。

- [x] **Step 2: 核对引用**

Run: `grep -rn "common.dto.UserFavoriteDTO" wealth-common wealth-service`
Expected: 无任何引用（common 内无引用、wealth-service 均引用 product 包同名类，互不干扰）

- [x] **Step 3: 编译**

Run: `mvn clean install -pl wealth-common -DskipTests`
Expected: BUILD SUCCESS

- [x] **Step 4: 汇报变更**

---

## 【阶段三】Task 7: L2 batch-read 补 @Valid

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/dto/BatchReadDTO.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/controller/MessageController.java:81-89`

- [x] **Step 1: BatchReadDTO 加校验**

`BatchReadDTO.java` 的 `ids` 字段：
```java
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** 待标记已读的消息ID列表 */
@NotEmpty(message = "消息ID列表不能为空")
private List<Long> ids;
```
（先 Read 该文件确认现有字段注解风格，保持一致）

- [x] **Step 2: Controller 改 @Valid 并删手写判空**

`MessageController.java` 的 `batchMarkAsRead`：

```java
@Operation(summary = "批量标记消息为已读")
@PutMapping("/batch-read")
public Result<Boolean> batchMarkAsRead(@Valid @RequestBody BatchReadDTO dto) {
    messageService.batchMarkAsRead(dto.getIds());
    return Result.success(true);
}
```

删除原来的 `if (dto.getIds() == null || dto.getIds().isEmpty()) { return Result.error(ResultCode.PARAM_ERROR); }`。检查 `ResultCode` import 是否因此变为未使用，若是则删除该 import。

- [x] **Step 3: 编译**

Run: `mvn clean install -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS

- [x] **Step 4: 测试**

Run: `mvn test -pl wealth-service -Dtest=MessageControllerTest -DskipTests=false`（若该测试类存在）
Expected: 通过（如缺正向/空参用例，按 CLAUDE.md 测试规范补 `batchMarkAsRead_should_validate_empty_ids` 等用例）

> 备注：`MessageControllerTest` 不存在，无既有用例需适配；`GlobalExceptionHandler` 已处理 `MethodArgumentNotValidException`，空 ids 由 200+PACK_ERROR 改为 400 校验错误，前端 `handleMarkAllRead` 已保证非空才调用，无回归。

- [x] **Step 5: 汇报变更**

---

## 【阶段三】Task 8: L3 SSE 例外文档化

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/controller/MarketDataController.java:41-46`
- Modify: `front/src/store/marketSSE.ts:12-19`
- Modify: `docs/ARCHITECTURE.md`

- [x] **Step 1: 后端 @Operation 标注载荷格式**

`MarketDataController.java` 的 `subscribe` 方法 `@Operation` 改为：
```java
@Operation(summary = "SSE 实时行情推送（返回原始 List<MarketDataVO> 数组，非 Result 信封；事件名 market-update）")
```

- [x] **Step 2: 前端注释**

`front/src/store/marketSSE.ts` 的 `onMessage` 上方加注释：
```ts
// 载荷为原始 MarketDataVO[] 数组（非 Result 信封），见 MarketDataController.subscribe
```

- [x] **Step 3: ARCHITECTURE.md 补约定**

在 `docs/ARCHITECTURE.md` 的接口/SSE 相关段落补一行：SSE `/product/wea-market-data/sse` 为全站唯一不包 `Result` 信封的接口，事件载荷为裸 `List<MarketDataVO>` 数组。

- [x] **Step 4: 汇报变更**

---

## 【阶段二】Task 9: L4 Long 主键 String 序列化（后端 VO + 前端类型放宽）

**Files:**
- Modify（后端 VO `id` 字段加 `@JsonSerialize`）：
  - `wealth-service/.../system/vo/UmsAdminVO.java`、`UmsRoleVO.java`、`UmsResourceVO.java`、`UmsAdminRoleRelationVO.java`、`UmsRoleResourceRelationVO.java`
  - `wealth-service/.../user/vo/UserVO.java`、`LoginVO.java`（`userId` 字段）
  - `wealth-service/.../message/vo/MessageVO.java`、`NewsVO.java`
  - `wealth-service/.../product/vo/MarketDataVO.java`、`ProductVO.java`、`UserFavoriteVO.java`
  - `wealth-service/.../trade/vo/TradeOrderVO.java`
- Modify: `front/src/types/index.ts`（id 类型放宽）
- Modify: `front/src/api/*.ts`（id 入参放宽）

- [x] **Step 1: 后端 VO id 加 @JsonSerialize**

对上述每个 VO 的 `id` 字段（`LoginVO` 为 `userId` 字段），在字段上增加注解：
```java
@JsonSerialize(using = ToStringSerializer.class)
private Long id;
```
并补 import：
```java
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
```

> 仅 `id`/`userId` 转 String；`total/count/volume/expiresInSeconds` 等保持数字，避免全局 Long→String 误伤数值字段。

- [x] **Step 2: 前端 types 放宽 id**

`front/src/types/index.ts`：所有 `id?: number`（UserInfo/UmsAdmin/UmsRole/UmsResource/WeaProduct/WeaMarketData/WeaTradeOrder/WeaUserFavorite/WeaNews/WeaMessage）改为 `id?: number | string`。

- [x] **Step 3: 前端 api 入参放宽**

`front/src/api/*.ts` 中所有 `(id: number)` 形式的函数签名（如 `getUserById(id: number)`、`deleteTradeOrder(id: number)`、`updateAdmin(id: number)` 等）改为 `(id: number | string)`。可批量用编辑器替换 `id: number` → `id: number | string`（仅限函数参数位置，勿改 `PageParam` 内 pageNum/pageSize 等）。

- [x] **Step 4: 类型检查**

Run: `cd front && npx vue-tsc --noEmit`
Expected: 0 错误

- [x] **Step 5: 编译 + 冒烟**

Run: `mvn clean install -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS
真实服务冒烟（`data.id` 为字符串）留待 Task 10。

- [x] **Step 6: 汇报变更**

---

## 【阶段三】Task 10: 汇总验证与收尾

- [x] **Step 1: 后端全量编译 + 全量测试**

Run: `mvn clean install -DskipTests=false`
Expected: BUILD SUCCESS，全部测试通过
实际：BUILD SUCCESS，wealth-common 9 + wealth-service 160 测试全通过。

- [x] **Step 2: 前端构建**

Run: `cd front && npx vue-tsc --noEmit && npx vite build`
Expected: 0 错误，构建成功
实际：vue-tsc 0 错误，vite build 成功。

- [x] **Step 3: 启动冒烟（已重启加载新构建）**

按 CLAUDE.md「启动验证」：gateway → wealth-service；重启 8080/8081 两个 java 进程（用户批准）后实测：
- `POST /user/identify-login`（admin/admin123）返回 JWT，`userId` 为字符串（L4）；
- 行情/资讯新增编辑：`marketTime=""` 不再 400（反序列化 H1 修复生效；`marketTime` 为 NOT NULL 列，空值触发 DB 500 属预期必填约束）；
- 新空格格式 `2026-08-03 15:00:00` 与旧 ISO `2026-08-03T15:00:00` 均 200 创建成功（H1 真实现场回归）；
- 分页响应含 `pageNum/pageSize/pages/total`、无 `current/size`（M1）；
- 行情 `id` 为字符串（L4）；
- batch-read 空 ids → 400「消息ID列表不能为空」（L2）；有效 ids → 200；
- riseFallRate=5 → 400「涨跌幅不能高于100%」（L5）；
- SSE `/product/wea-market-data/sse` 返回 `event:market-update` + 裸数组载荷（L3）。

> **冒烟发现并修复的回归（重要）**：`JacksonConfig` 在 wealth-common 直接 `serializerByType(Page.class, ...)`，而 gateway 依赖 wealth-common 但**无 mybatis-plus 依赖**（无数据源），启动报 `NoClassDefFoundError: com/baomidou/mybatisplus/extension/plugins/pagination/Page`。已修复：Page 注册拆为独立 bean，加 `@ConditionalOnClass(name="com.baomidou.mybatisplus.extension.plugins.pagination.Page")`（与 `MyBatisPlusConfig` 同模式），gateway 跳过、service 生效。该缺陷单元测试/全量编译无法暴露，仅启动冒烟可发现。

- [ ] **Step 4: 汇总全部变更，等待用户确认后提交**

按 Git 规范分类型提交（feat/fix），或按用户指定方式合并提交。所有 commit 需用户明确指令。

---

## 自检记录

- **Spec 覆盖**：H1/M3→Task1+2、H2→Task5、M1→Task3、M2→Task4、L1→Task6、L2→Task7、L3→Task8、L4→Task9、L5→Task4。10 项全覆盖。
- **占位符扫描**：无 TBD/TODO；每步含具体代码或命令。
- **类型一致性**：`FlexibleLocalDateTimeDeserializer`/`PageSerializer` 在 Task1 定义并在 JacksonConfig 注册；`ApiClient` 在 Task5 定义并被 api 函数使用；`number | string` 在 Task9 贯穿 types 与 api 签名。
