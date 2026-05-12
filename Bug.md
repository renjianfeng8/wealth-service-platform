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
