# Bug 记录文档

> 记录项目开发中遇到的关键问题及其解决方案，供后续排查参考。

---

## Bug-001: ES 搜索报 ConversionException（日期格式不匹配）

**日期**: 2026-05-12
**模块**: finance-search
**影响**: ES 搜索接口返回 500，无法查询数据

### 现象

调用 `GET /search/product/search?keyword=xxx` 返回：

```json
{"code":500,"message":"系统错误：Conversion exception when converting document id 1"}
```

但 ES 集群本身查询正常（docker exec 直接查询 ES 成功），索引文档也存在（count=8）。

### 根因

`ProductDocument.java` 中 `createTime` 和 `updateTime` 字段定义：

```java
@Field(type = FieldType.Date)
private LocalDateTime createTime;
```

未指定日期格式时，Spring Data Elasticsearch 默认使用 `date_optional_time` 格式存储。ES 返回的 `_source` 中日期被截断为纯日期字符串（如 `"2026-05-10"`），但 Java 实体字段类型为 `LocalDateTime`，反序列化时无法将 `"2026-05-10"` 转换为 `LocalDateTime`，抛出 `ConversionException`。

### 修复

显式指定日期格式为 `DateFormat.date_hour_minute_second_millis`：

```java
@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
private LocalDateTime createTime;
```

同时索引数据时须传入完整 ISO 日期时间格式（如 `"2026-05-10T11:29:46.000"`）。

### 涉及文件

- `finance-search/src/main/java/com/finance/platform/search/entity/ProductDocument.java`

### 排查要点（后续遇到类似问题先查此清单）

- [ ] ES 查询是否报 `ConversionException` / 搜索接口返回 500
- [ ] ES mapping 中日期字段格式是否与 Java 实体 `@Field` 声明一致
- [ ] 索引文档的 `_source` 中日期值是否为完整格式（含时间部分）
- [ ] `@Field` 中 `FieldType` 是否与 Java 类型匹配（如 `BigDecimal` ↔ `Scaled_Float`）
- [ ] 无 Redis 依赖的模块启动是否报 `NoClassDefFoundError: RedisSerializer`
      → 检查 `RedisConfig` / `RedisUtil` 是否有 `@ConditionalOnClass`
- [ ] IK 分词器是否生效 → 检查 ES mapping 中 `analyzer` 是否为 `ik_max_word`

---

## Bug-002: finance-search 启动失败（RedisSerializer NoClassDefFoundError）

**日期**: 2026-05-12
**模块**: finance-common / finance-search

### 现象

finance-search 启动时报：

```
NoClassDefFoundError: org.springframework.data.redis.serializer.RedisSerializer
```

### 根因

`RedisConfig.java` 和 `RedisUtil.java` 位于 finance-common 中，但 finance-search 在 pom.xml 中排除了 Redis 依赖（`spring-boot-starter-data-redis`）。Spring 启动时扫描到这两个类并尝试加载，因缺少 Redis 类而失败。

### 修复

在两个类上添加 `@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`：

```java
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisConfig { ... }

@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisUtil { ... }
```

### 涉及文件

- `finance-common/src/main/java/com/finance/common/config/RedisConfig.java`
- `finance-common/src/main/java/com/finance/common/utils/RedisUtil.java`

---

## Bug-003: ES 索引数据为空（索引重建后未同步）

**日期**: 2026-05-12
**模块**: finance-search / finance-product

### 现象

ES 索引 `finance_product` 存在但文档数为 0，搜索无结果。

### 根因

ES 索引被删除重建后，MySQL 中的产品数据未自动同步到 ES。项目目前无自动同步机制，需手动通过 search 服务的 save API 重新索引。

### 重建步骤

```bash
# 1. 从产品服务获取所有产品
curl http://localhost:8080/product/finProduct

# 2. 逐条通过 search 服务写入 ES
# POST http://localhost:8080/search/product
# Body: { "id":1, "productName":"黄金ETF", "productCode":"GOLD001", ... }

# 3. 验证
docker exec es curl -s 'http://localhost:9200/finance_product/_count'
```

### 涉及文件

- `finance-search/src/main/java/com/finance/platform/search/controller/ProductSearchController.java`
- `finance-product/src/main/java/com/finance/platform/product/controller/FinProductController.java`

---

## Bug-004: 交易委托提交提示"用户信息异常"（userId 为 0）

**日期**: 2026-05-12
**模块**: front-user / finance-user
**影响**: 登录后无法提交交易委托单

### 现象

用户已登录（持有 JWT Token，能正常访问各页面），但提交交易委托时弹窗提示"用户信息异常，请重新登录"。Playwright 测试全部通过（34项），仅手动提交流程触发该错误。

### 根因

`front-user/src/store/index.ts` 中 `login()` 方法流程：

```
登录成功 → 获取 token → setToken() → 调用 getUserList() 查询所有用户
→ users.find(u => u.username === 登录用户名) → 匹配到则设置 userId
```

`getUserList()` 发 GET `/user` 请求依赖后端拦截器验证 Token，若该请求因任何原因失败（网络超时、服务未就绪、Token 校验异常等），`catch` 块静默吞掉错误，`userId` 保持为 0。`setStoredUser({ userId: 0, ... })` 将 0 写入 localStorage。后续页面 reload 后 `userId` 依然是 0。

交易委托页 `handleSubmit()` 检查 `if (!userStore.userId)` → `!0 === true` → 显示"用户信息异常"。

### 修复

**方案**：登录接口不再返回纯字符串 Token，改为返回 `LoginVO { token, userId, nickname }`，前端直接从登录响应中获取 userId，消除对 `getUserList()` 的二次调用依赖。

#### 后端改动

1. 新增 `LoginVO`（`finance-user/vo/LoginVO.java`）
   ```java
   public class LoginVO {
       private String token;
       private Long userId;
       private String nickname;
   }
   ```

2. `UserService.login()` 返回类型从 `String` 改为 `LoginVO`
3. `UserController.login()` 返回类型从 `Result<String>` 改为 `Result<LoginVO>`

#### 前端改动

`front-user/src/store/index.ts` 中 `login()`：

```typescript
// 之前：取 token 后二次调用 getUserList()
const token = res.data as string
this.token = token
setToken(token)
// getUserList() 可能失败...

// 之后：直接从登录响应解构 token + userId
const { token, userId } = res.data
this.token = token
this.userId = userId
setToken(token)
setStoredUser({ username, userId, nickname, avatar })
```

### 排查要点（添加到已有清单）

- [ ] 前端"用户信息异常" → 检查 `userStore.userId` 是否为 0
- [ ] 检查登录接口响应中是否包含 `userId`
- [ ] 检查 localStorage 中 `finance_user_info.userId` 值
- [ ] 更新代码后须重启 finance-user 服务使 VO 变更生效

### 涉及文件

- `finance-user/src/main/java/com/finance/user/vo/LoginVO.java`（新增）
- `finance-user/src/main/java/com/finance/user/service/UserService.java`
- `finance-user/src/main/java/com/finance/user/service/impl/UserServiceImpl.java`
- `finance-user/src/main/java/com/finance/user/controller/UserController.java`
- `front-user/src/store/index.ts`

---

## Bug-005: 交易委托分页筛选不生效（orderStatus 参数被忽略）

**日期**: 2026-05-12
**模块**: finance-trade
**影响**: 前端筛选"已成交/待成交/已撤销"无效果，始终返回全部数据

### 现象

前端委托单列表的筛选下拉框选择"已成交"或"已撤销"后，列表数据未变化，始终展示全部订单。浏览器 Network 面板可看到 `orderStatus` 参数已正常发送。

### 根因

`FinTradeOrderController.page()` 方法只接收 `pageNum` 和 `pageSize` 两个参数，未声明 `orderStatus` 和 `userId` 参数。`orderStatus` 和 `userId` 虽以 query string 形式发送到后端，但被 Spring MVC 忽略。

```java
// 修复前：只有分页参数，无筛选参数
@GetMapping("/page")
public Result<IPage<FinTradeOrderVO>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<FinTradeOrder> page = new Page<>(pageNum, pageSize);
    IPage<FinTradeOrder> entityPage = finTradeOrderService.page(page); // 无条件查询全部
    ...
}
```

### 涉及文件

- `finance-trade/src/main/java/com/finance/platform/trade/controller/FinTradeOrderController.java`
- `finance-trade/src/main/java/com/finance/platform/trade/service/FinTradeOrderService.java`
- `finance-trade/src/main/java/com/finance/platform/trade/service/impl/FinTradeOrderServiceImpl.java`

---

## Bug-006: 产品中心分类筛选不生效（productType 参数被忽略）

**日期**: 2026-05-12
**模块**: finance-product
**影响**: 前端筛选"贵金属/理财产品/基金/股票"无效果，始终显示全部产品

### 根因

与 Bug-005 相同模式 — `FinProductController.page()` 只接收 `pageNum` 和 `pageSize`，未声明 `productType` 参数，前端传参被 Spring MVC 忽略。

### 涉及文件

- `finance-product/src/main/java/com/finance/platform/product/controller/FinProductController.java`
- `finance-product/src/main/java/com/finance/platform/product/service/FinProductService.java`
- `finance-product/src/main/java/com/finance/platform/product/service/impl/FinProductServiceImpl.java`

---

## Bug-007: 财经资讯/消息中心分类筛选不生效（newsType/userId 参数被忽略）

**日期**: 2026-05-12
**模块**: finance-message
**影响**: 财经资讯的分类筛选（行业动态/市场分析/政策解读/公司公告）和消息中心的用户筛选不生效

### 根因

与 Bug-005/Bug-006 相同模式 — `FinNewsController.page()` 和 `FinMessageController.page()` 只接收分页参数，未声明 `newsType`/`userId` 筛选参数。

### 涉及文件

- `finance-message/src/main/java/com/finance/platform/message/controller/FinNewsController.java`
- `finance-message/src/main/java/com/finance/platform/message/service/FinNewsService.java`
- `finance-message/src/main/java/com/finance/platform/message/service/impl/FinNewsServiceImpl.java`
- `finance-message/src/main/java/com/finance/platform/message/controller/FinMessageController.java`
- `finance-message/src/main/java/com/finance/platform/message/service/FinMessageService.java`
- `finance-message/src/main/java/com/finance/platform/message/service/impl/FinMessageServiceImpl.java`

---

## Bug-008: 停售产品仍可点击"去交易"跳转交易页

**日期**: 2026-05-12
**模块**: front-user
**影响**: 标记为"停售"的产品，用户仍可通过详情弹窗中的"去交易"按钮进入交易页下单

### 现象

产品卡片上显示"停售"标签的产品，点击查看详情后，详情弹窗底部的"去交易"按钮仍可点击，会跳转到交易委托页并带入产品代码，用户可能对停售产品下单。

### 根因

详情弹窗的"去交易"按钮未根据 `status` 字段做条件禁用，始终可点击：

```html
<!-- 修复前：始终可点击 -->
<el-button type="primary" @click="goTrade(detailItem)">去交易</el-button>

<!-- 修复后：停售时禁用 -->
<el-button type="primary" :disabled="detailItem?.status !== 1" @click="goTrade(detailItem)">去交易</el-button>
```

### 涉及文件

- `front-user/src/views/product/index.vue`
