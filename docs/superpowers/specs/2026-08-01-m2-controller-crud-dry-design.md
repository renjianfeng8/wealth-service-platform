# M2 · Controller 层 CRUD 样板重复修复设计

> 状态：已获用户确认（2026-08-01）
> 依据：《阿里巴巴 Java 开发手册》DRY / 分层规约 + 本项目 CODE-STANDARDS.md §5.1 / §八

## 背景

全库约 30 处 Controller 重复 `getById → if (null) → Result.error(NOT_FOUND) → convert → Result.success` 模板，同时存在 404 双轨制：

- Controller 路径：`Result.error(ResultCode.NOT_FOUND)`（body `{code:404, message:"资源不存在"}`）
- Service 路径：`BaseBizServiceImpl.updateDto/deleteWithCheck` 抛 `ServiceException(404, "X不存在")`，由 `GlobalExceptionHandler` 映射为 `{code:404, message}`

两种风格并存，判空规范分裂。且 `NewsController`/`MessageController` 的 `if (!success) return Result.error(NOT_FOUND)` 为死代码——Service 的 `updateDto`/`deleteWithCheck` 在实体缺失时已抛异常而非返回 false。

## 目标（非目标）

**目标**：消除约 30 处 null-check 样板；统一 404 到唯一异常路径；Controller 收敛为仅路由。
**非目标**：不改表结构、不改分页/新增接口签名（各域差异大，不强行模板化）、不做关联表缓存逻辑下沉、不动 Frontend。

## 方案：Service 层 404 标准化 + Controller 瘦身

### 1. `BaseBizServiceImpl` 新增两个模板方法

复用现有 `getVoById` / `getById`，风格与 `updateDto` / `deleteWithCheck` 一致（`throw new ServiceException(404, entityName + "不存在")`）：

```java
/** 根据 ID 查询并转换为 VO，不存在时抛 404。 */
protected <V> V getVoByIdOrThrow(Long id, Class<V> voClass, String entityName) {
    V vo = getVoById(id, voClass);
    if (vo == null) {
        throw new ServiceException(404, entityName + "不存在");
    }
    return vo;
}

/** 根据 ID 查询实体，不存在时抛 404（供更新/删除后仍需原实体的场景使用）。 */
protected E getEntityOrThrow(Long id, String entityName) {
    E entity = getById(id);
    if (entity == null) {
        throw new ServiceException(404, entityName + "不存在");
    }
    return entity;
}
```

注：`getVoById` 保留 null 契约不变（仅 6 个 getter 内部使用，无不兼容调用方）。

### 2. Service 层改动

**Category B —— 已返回 VO 的 getter 改为抛 404（6 个实现）**

`ProductServiceImpl.getProductById`、`MarketDataServiceImpl.getMarketDataById`、`NewsServiceImpl.getNewsById`、`MessageServiceImpl.getMessageById`、`TradeOrderServiceImpl.getOrderById`、`UserFavoriteServiceImpl.getFavoriteById`：`getVoById(...)` → `getVoByIdOrThrow(..., "实体名")`。

实体名：产品 / 行情数据 / 资讯 / 消息 / 交易委托单 / 自选关注。

`MessageServiceImpl.markAsRead`：`getById → return false` 改为 `getEntityOrThrow(id, "消息")` 抛 404（消除其 Controller 的 404 判断）。

**Category A —— 补齐薄 CRUD 方法（接口 + 实现）**

| Service | 新增方法 | 实现要点 |
|---|---|---|
| `UmsAdminService` | `getAdminById` / `updateAdmin` / `deleteAdmin` | `getVoByIdOrThrow(…, "管理员")`；update：`getEntityOrThrow` → `copyNonNullProperties` → **清空密码** → `updateById`；delete：`deleteWithCheck`。`clearPasswordForUpdate` 下沉到 Service，Controller 私有方法删除 |
| `UserService` | `getUserById` / `updateUser` / `deleteUser` | 同上（"用户"） |
| `UmsRoleService` | `getRoleById` / `updateRole` / `deleteRole` | `UmsRoleServiceImpl` 父类改为 `BaseBizServiceImpl`；update/delete 走 `updateDto` / `deleteWithCheck`（"角色"） |
| `UmsResourceService` | `getResourceById` / `updateResource` / `deleteResource` | `UmsResourceServiceImpl` 父类改为 `BaseBizServiceImpl`（"后台资源"） |
| `UmsAdminRoleRelationService` | `getAdminRoleRelationById` + `getAdminRoleRelationEntityOrThrow` | VO getter 走 `getVoByIdOrThrow`（"管理员角色关联"）；entity getter 走 `getEntityOrThrow`（供 Controller 缓存逻辑） |
| `UmsRoleResourceRelationService` | `getRoleResourceRelationById` + `getRoleResourceRelationEntityOrThrow` | 同上（"角色资源关联"） |

### 3. Controller 层改动（10 个文件）

- **6 个 Category B**：`getById` 变 `return Result.success(service.getXxxById(id));`；News/Message 的 update/delete 删除死代码 404 判断，改为直接返回 `Result.success(service.updateXxx/deleteXxx(...))`。
- **4 个 Category A**（UmsAdmin/User/UmsRole/UmsResource）：getById/update/delete 全部委托新增 Service 方法。
- **2 个关联表 Controller**（UmsAdminRoleRelation / UmsRoleResourceRelation）：**保留权限缓存清理逻辑**，仅将 `getById → if null → Result.error(NOT_FOUND)` 替换为 `service.getXxxEntityOrThrow(id)` 调用（`getAdminRoleRelationEntityOrThrow` / `getRoleResourceRelationEntityOrThrow`，内部抛 404）。

### 4. 404 语义

所有 404 统一 `ServiceException(404, "X不存在")` → `GlobalExceptionHandler.handleServiceException` → body `{code:404, message:"X不存在"}`（HTTP 仍 200）。**双轨制消除**，消息改为实体化文案。Frontend 不匹配 404 文案，无影响。

### 5. 测试（TDD）

- 4 个现有测试改断言：`ProductServiceImplTest` / `NewsServiceImplTest` / `MessageServiceImplTest` / `TradeOrderServiceImplTest` 的 `getXxxById_NotFound` 由 `assertNull` 改为 `assertThrows(ServiceException.class, ...)` + 校验 `code==404`。
- 新增 Category A 服务方法补正/负用例（`getXxxById` 存在返回 VO、不存在抛 404；update/delete 同）。

### 6. 验证

1. `mvn test -pl wealth-service -DskipTests=false`
2. `mvn clean install -pl wealth-common -DskipTests`（若 common 有改动）
3. 全量编译 `mvn clean install -DskipTests`

## 涉及文件清单

- `BaseBizServiceImpl`（+2 方法）
- Service 接口（6）：UmsAdminService、UserService、UmsRoleService、UmsResourceService、UmsAdminRoleRelationService、UmsRoleResourceRelationService
- Service 实现（12）：上述 6 个实现 + Product/MarketData/News/Message/TradeOrder/UserFavorite 的 getter 改动
- Controller（10）：全部
- 测试（4+）：ProductServiceImplTest、NewsServiceImplTest、MessageServiceImplTest、TradeOrderServiceImplTest 改；Category A 新增

## 风险与注意

- `getXxxById` 语义变化（null → 抛 404）：对外响应体不变，仅内部契约变化；已枚举 4 个受影响测试。
- `UmsRole` / `UmsResource` Service 父类变更需确认无其他依赖（二者现仅 `ServiceImpl` 能力）。
- 实体名文案需与现有 `updateDto`/`deleteWithCheck` 保持一致风格（中文 + "不存在"）。
- 禁止改动：init.sql、application.yml、pom.xml、Frontend。
