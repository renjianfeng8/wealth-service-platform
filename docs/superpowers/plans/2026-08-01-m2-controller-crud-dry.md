# M2 Controller CRUD 样板 DRY 修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 消除全库约 30 处 Controller 层 getById/update/delete 的 `getById → if null → Result.error(NOT_FOUND)` 样板，统一 404 到 `ServiceException` 单一异常路径，Controller 收敛为仅路由。

**Architecture:** 在 `BaseBizServiceImpl` 增加 `getVoByIdOrThrow` / `getEntityOrThrow` 模板；各域 Service 的 `getXxxById` 改为抛 404（与已有 `updateDto`/`deleteWithCheck` 一致），补齐 Category A 的薄 CRUD 方法；10 个 Controller 的 getById/update/delete 改为一行委托。404 全部经 `GlobalExceptionHandler` 输出 `{code:404, message:"X不存在"}`。

**Tech Stack:** Java 21 · Spring Boot 3.3.13 · MyBatis-Plus 3.5.9 · JUnit 5 + Mockito

> **提交约定**：本项目 CLAUDE.md 禁止自动 `git commit`/`git push`，须用户明确指令。本计划以「验证点」替代 commit 步骤。若 `mvn test -pl wealth-service` 报找不到 `wealth-common`，先执行 `mvn clean install -pl wealth-common -DskipTests`。

---

## 文件结构总览

| 文件 | 变更 |
|------|------|
| `wealth-service/.../common/base/BaseBizServiceImpl.java` | +`getVoByIdOrThrow` / `getEntityOrThrow` |
| `wealth-service/.../product/service/impl/ProductServiceImpl.java` | `getProductById` → 抛 404 |
| `wealth-service/.../message/service/impl/NewsServiceImpl.java` | `getNewsById` → 抛 404 |
| `wealth-service/.../message/service/impl/MessageServiceImpl.java` | `getMessageById` → 抛 404；`markAsRead` → 抛 404 |
| `wealth-service/.../trade/service/impl/TradeOrderServiceImpl.java` | `getOrderById` → 抛 404 |
| `wealth-service/.../product/service/impl/MarketDataServiceImpl.java` | `getMarketDataById` → 抛 404 |
| `wealth-service/.../product/service/impl/UserFavoriteServiceImpl.java` | `getFavoriteById` → 抛 404 |
| `wealth-service/.../{system,user}/service/*Service.java`（6 接口） | +薄 CRUD 方法 |
| `wealth-service/.../{system,user}/service/impl/*ServiceImpl.java`（6 实现） | +薄 CRUD 方法 / 父类改 `BaseBizServiceImpl` |
| 10 个 Controller | getById/update/delete 一行委托，删 `ResultCode` 未用导入 |
| 测试 | 改 4 个 NotFound 断言 + 新增 4 个 Service 测试类 + 扩 2 个 Service 测试类 |

---

## Task 1: BaseBizServiceImpl 新增模板方法 + 测试

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/common/base/BaseBizServiceImpl.java`
- Create: `wealth-service/src/test/java/com/wealth/platform/common/base/BaseBizServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

创建 `BaseBizServiceImplTest.java`：

```java
package com.wealth.platform.common.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wealth.common.entity.BaseEntity;
import com.wealth.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseBizServiceImplTest {

    interface TestMapper extends BaseMapper<BaseEntity> {}

    static class TestServiceImpl extends BaseBizServiceImpl<TestMapper, BaseEntity> {}

    @Mock
    private TestMapper testMapper;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = spy(new TestServiceImpl());
        ReflectionTestUtils.setField(testService, "baseMapper", testMapper);
    }

    @Test
    @DisplayName("getVoByIdOrThrow-不存在抛404")
    void getVoByIdOrThrow_whenMissing_throws404() {
        when(testMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> testService.getVoByIdOrThrow(99L, BaseEntity.class, "测试"));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getEntityOrThrow-不存在抛404")
    void getEntityOrThrow_whenMissing_throws404() {
        when(testMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> testService.getEntityOrThrow(99L, "测试"));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("getEntityOrThrow-存在返回实体")
    void getEntityOrThrow_whenFound_returnsEntity() {
        BaseEntity entity = new BaseEntity();
        entity.setId(1L);
        when(testMapper.selectById(1L)).thenReturn(entity);

        BaseEntity result = testService.getEntityOrThrow(1L, "测试");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=BaseBizServiceImplTest -DskipTests=false`
Expected: 编译失败 — `getVoByIdOrThrow` / `getEntityOrThrow` 方法不存在。

- [ ] **Step 3: 实现**

在 `BaseBizServiceImpl.java` 中，`deleteWithCheck` 方法之后新增：

```java
    /**
     * 根据 ID 查询并转换为 VO，不存在时抛 404（与 updateDto/deleteWithCheck 统一 404 语义）。
     *
     * @param id         主键
     * @param voClass    VO 类型
     * @param entityName 实体中文名（异常消息用）
     */
    protected <V> V getVoByIdOrThrow(Long id, Class<V> voClass, String entityName) {
        V vo = getVoById(id, voClass);
        if (vo == null) {
            throw new ServiceException(404, entityName + "不存在");
        }
        return vo;
    }

    /**
     * 根据 ID 查询实体，不存在时抛 404（供更新/删除后仍需原实体的场景使用）。
     *
     * @param id         主键
     * @param entityName 实体中文名（异常消息用）
     */
    protected E getEntityOrThrow(Long id, String entityName) {
        E entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, entityName + "不存在");
        }
        return entity;
    }
```

`ServiceException` 已 import（第 10 行）。无需新增导入。

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=BaseBizServiceImplTest -DskipTests=false`
Expected: 3 tests PASS。

- [ ] **Step 5: 验证点**

`mvn compile -pl wealth-service -DskipTests` 通过。

---

## Task 2: ProductServiceImpl.getProductById → 抛 404

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/service/impl/ProductServiceImpl.java:24-26`
- Modify: `wealth-service/src/test/java/com/wealth/platform/product/service/impl/ProductServiceImplTest.java`

- [ ] **Step 1: 改失败测试**

`ProductServiceImplTest.java` 中，将 `getProductById_NotFound`（约第 72-80 行）整体替换为：

```java
    @Test
    @DisplayName("根据ID查询产品-不存在抛404")
    void getProductById_NotFound() {
        when(productMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> productService.getProductById(99L));

        assertEquals(404, ex.getCode());
    }
```

（`ServiceException` / `assertThrows` / `assertEquals` 均已 import。删除不再使用的 `import static org.junit.jupiter.api.Assertions.assertNull;`，可选。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=ProductServiceImplTest -DskipTests=false`
Expected: `getProductById_NotFound` FAIL — 当前实现返回 null 未抛异常。

- [ ] **Step 3: 实现**

`ProductServiceImpl.java` 中：

```java
    @Override
    public ProductVO getProductById(Long id) {
        return getVoByIdOrThrow(id, ProductVO.class, "产品");
    }
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=ProductServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 3: NewsServiceImpl.getNewsById → 抛 404

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/service/impl/NewsServiceImpl.java:25-27`
- Modify: `wealth-service/src/test/java/com/wealth/platform/message/service/impl/NewsServiceImplTest.java`

- [ ] **Step 1: 改失败测试**

`NewsServiceImplTest.java` 中，将 `getNewsById_NotFound`（约第 71-78 行）整体替换为：

```java
    @Test
    @DisplayName("根据ID查询资讯-不存在抛404")
    void getNewsById_NotFound() {
        when(newsMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> newsService.getNewsById(99L));

        assertEquals(404, ex.getCode());
    }
```

（删除不再使用的 `import static org.junit.jupiter.api.Assertions.assertNull;`，可选。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=NewsServiceImplTest -DskipTests=false`
Expected: `getNewsById_NotFound` FAIL。

- [ ] **Step 3: 实现**

```java
    @Override
    public NewsVO getNewsById(Long id) {
        return getVoByIdOrThrow(id, NewsVO.class, "资讯");
    }
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=NewsServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 4: MessageServiceImpl（getMessageById + markAsRead → 抛 404）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/service/impl/MessageServiceImpl.java:27-29,71-76`
- Modify: `wealth-service/src/test/java/com/wealth/platform/message/service/impl/MessageServiceImplTest.java`

- [ ] **Step 1: 改失败测试**

`MessageServiceImplTest.java`：
1. 将 `getMessageById_NotFound`（约第 91-99 行）整体替换为：

```java
    @Test
    @DisplayName("根据ID查询消息-不存在抛404")
    void getMessageById_NotFound() {
        when(messageMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> messageService.getMessageById(99L));

        assertEquals(404, ex.getCode());
    }
```

2. 在类末尾（`deleteMessage_Success` 之后）追加两个测试：

```java
    @Test
    @DisplayName("标记消息已读-成功")
    void markAsRead_Success() {
        when(messageMapper.selectById(1L)).thenReturn(mockMessage);
        when(messageMapper.updateById(any(WeaMessage.class))).thenReturn(1);

        boolean result = messageService.markAsRead(1L);

        assertTrue(result);
        verify(messageMapper).updateById(argThat(msg ->
                msg.getId() == 1L && 1 == msg.getReadFlag()
        ));
    }

    @Test
    @DisplayName("标记消息已读-不存在抛404")
    void markAsRead_NotFound() {
        when(messageMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> messageService.markAsRead(99L));
        verify(messageMapper, never()).updateById(isA(WeaMessage.class));
    }
```

（所需 import 均已存在：`ServiceException`、`assertThrows`、`assertTrue`、`any`、`argThat`、`never`、`isA`、`verify`。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=MessageServiceImplTest -DskipTests=false`
Expected: `getMessageById_NotFound` 与 `markAsRead_NotFound` FAIL。

- [ ] **Step 3: 实现**

```java
    @Override
    public MessageVO getMessageById(Long id) {
        return getVoByIdOrThrow(id, MessageVO.class, "消息");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long id) {
        WeaMessage entity = getEntityOrThrow(id, "消息");
        entity.setReadFlag(1);
        return updateById(entity);
    }
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=MessageServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 5: TradeOrderServiceImpl.getOrderById → 抛 404

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/trade/service/impl/TradeOrderServiceImpl.java:53-55`
- Modify: `wealth-service/src/test/java/com/wealth/platform/trade/service/impl/TradeOrderServiceImplTest.java`

- [ ] **Step 1: 改失败测试**

`TradeOrderServiceImplTest.java` 中，将 `getOrderById_NotFound`（约第 144-152 行）整体替换为：

```java
    @Test
    @DisplayName("根据ID查询订单-不存在抛404")
    void getOrderById_NotFound() {
        when(tradeOrderMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> tradeOrderService.getOrderById(99L));

        assertEquals(404, ex.getCode());
    }
```

（删除不再使用的 `import static org.junit.jupiter.api.Assertions.assertNull;`，可选。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=TradeOrderServiceImplTest -DskipTests=false`
Expected: `getOrderById_NotFound` FAIL。

- [ ] **Step 3: 实现**

```java
    @Override
    public TradeOrderVO getOrderById(Long id) {
        return getVoByIdOrThrow(id, TradeOrderVO.class, "交易委托单");
    }
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=TradeOrderServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 6: MarketData + UserFavorite getter → 抛 404（新增测试类）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/service/impl/MarketDataServiceImpl.java:31-33`
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/service/impl/UserFavoriteServiceImpl.java:44-46`
- Create: `wealth-service/src/test/java/com/wealth/platform/product/service/impl/MarketDataServiceImplTest.java`
- Create: `wealth-service/src/test/java/com/wealth/platform/product/service/impl/UserFavoriteServiceImplTest.java`

- [ ] **Step 1: 写失败测试（MarketData）**

创建 `MarketDataServiceImplTest.java`：

```java
package com.wealth.platform.product.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.MarketDataMapper;
import com.wealth.platform.product.vo.MarketDataVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceImplTest {

    @Mock
    private MarketDataMapper marketDataMapper;

    private MarketDataServiceImpl marketDataService;

    private WeaMarketData mockData;

    @BeforeEach
    void setUp() {
        marketDataService = spy(new MarketDataServiceImpl());
        ReflectionTestUtils.setField(marketDataService, "baseMapper", marketDataMapper);

        mockData = new WeaMarketData();
        mockData.setId(1L);
        mockData.setProductCode("P001");
        mockData.setCurrentPrice(new BigDecimal("100.00"));
        mockData.setMarketTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据ID查询行情数据-成功")
    void getMarketDataById_Found() {
        when(marketDataMapper.selectById(1L)).thenReturn(mockData);

        MarketDataVO result = marketDataService.getMarketDataById(1L);

        assertNotNull(result);
        assertEquals("P001", result.getProductCode());
    }

    @Test
    @DisplayName("根据ID查询行情数据-不存在抛404")
    void getMarketDataById_NotFound() {
        when(marketDataMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> marketDataService.getMarketDataById(99L));

        assertEquals(404, ex.getCode());
    }
}
```

- [ ] **Step 2: 运行测试确认失败（MarketData）**

Run: `mvn test -pl wealth-service -Dtest=MarketDataServiceImplTest -DskipTests=false`
Expected: `getMarketDataById_NotFound` FAIL。

- [ ] **Step 3: 写失败测试（UserFavorite）**

创建 `UserFavoriteServiceImplTest.java`：

```java
package com.wealth.platform.product.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.mapper.UserFavoriteMapper;
import com.wealth.platform.product.vo.UserFavoriteVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFavoriteServiceImplTest {

    @Mock
    private UserFavoriteMapper userFavoriteMapper;

    private UserFavoriteServiceImpl userFavoriteService;

    private WeaUserFavorite mockFavorite;

    @BeforeEach
    void setUp() {
        userFavoriteService = spy(new UserFavoriteServiceImpl());
        ReflectionTestUtils.setField(userFavoriteService, "baseMapper", userFavoriteMapper);

        mockFavorite = new WeaUserFavorite();
        mockFavorite.setId(1L);
        mockFavorite.setUserId(100L);
        mockFavorite.setProductCode("P001");
    }

    @Test
    @DisplayName("根据ID查询自选-成功")
    void getFavoriteById_Found() {
        when(userFavoriteMapper.selectById(1L)).thenReturn(mockFavorite);

        UserFavoriteVO result = userFavoriteService.getFavoriteById(1L);

        assertNotNull(result);
        assertEquals("P001", result.getProductCode());
    }

    @Test
    @DisplayName("根据ID查询自选-不存在抛404")
    void getFavoriteById_NotFound() {
        when(userFavoriteMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userFavoriteService.getFavoriteById(99L));

        assertEquals(404, ex.getCode());
    }
}
```

- [ ] **Step 4: 运行测试确认失败（UserFavorite）**

Run: `mvn test -pl wealth-service -Dtest=UserFavoriteServiceImplTest -DskipTests=false`
Expected: `getFavoriteById_NotFound` FAIL。

- [ ] **Step 5: 实现两个 getter**

```java
    // MarketDataServiceImpl
    @Override
    public MarketDataVO getMarketDataById(Long id) {
        return getVoByIdOrThrow(id, MarketDataVO.class, "行情数据");
    }
```

```java
    // UserFavoriteServiceImpl
    @Override
    public UserFavoriteVO getFavoriteById(Long id) {
        return getVoByIdOrThrow(id, UserFavoriteVO.class, "自选关注");
    }
```

- [ ] **Step 6: 运行两个测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=MarketDataServiceImplTest,UserFavoriteServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 7: UmsRoleService 薄 CRUD（接口 + 实现 + 父类改造 + 测试）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/UmsRoleService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsRoleServiceImpl.java`
- Create: `wealth-service/src/test/java/com/wealth/platform/system/service/impl/UmsRoleServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

创建 `UmsRoleServiceImplTest.java`：

```java
package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.mapper.UmsRoleMapper;
import com.wealth.platform.system.vo.UmsRoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsRoleServiceImplTest {

    @Mock
    private UmsRoleMapper umsRoleMapper;

    private UmsRoleServiceImpl umsRoleService;

    private UmsRole mockRole;

    @BeforeEach
    void setUp() {
        umsRoleService = spy(new UmsRoleServiceImpl());
        ReflectionTestUtils.setField(umsRoleService, "baseMapper", umsRoleMapper);

        mockRole = new UmsRole();
        mockRole.setId(1L);
        mockRole.setName("超级管理员");
        mockRole.setStatus(1);
        mockRole.setSort(1);
    }

    @Test
    @DisplayName("根据ID查询角色-成功")
    void getRoleById_Found() {
        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);

        UmsRoleVO result = umsRoleService.getRoleById(1L);

        assertNotNull(result);
        assertEquals("超级管理员", result.getName());
    }

    @Test
    @DisplayName("根据ID查询角色-不存在抛404")
    void getRoleById_NotFound() {
        when(umsRoleMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> umsRoleService.getRoleById(99L));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("更新角色成功")
    void updateRole_Success() {
        UmsRoleDTO dto = new UmsRoleDTO();
        dto.setName("更新后的角色");

        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);
        when(umsRoleMapper.updateById(any(UmsRole.class))).thenReturn(1);

        boolean result = umsRoleService.updateRole(1L, dto);

        assertTrue(result);
        verify(umsRoleMapper).updateById(argThat(role ->
                role.getId() == 1L && "更新后的角色".equals(role.getName())
        ));
    }

    @Test
    @DisplayName("更新角色-不存在抛404")
    void updateRole_NotFound() {
        when(umsRoleMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> umsRoleService.updateRole(99L, new UmsRoleDTO()));
        verify(umsRoleMapper, never()).updateById(isA(UmsRole.class));
    }

    @Test
    @DisplayName("删除角色成功")
    void deleteRole_Success() {
        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);
        when(umsRoleMapper.deleteById(1L)).thenReturn(1);

        boolean result = umsRoleService.deleteRole(1L);

        assertTrue(result);
        verify(umsRoleMapper).deleteById(1L);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=UmsRoleServiceImplTest -DskipTests=false`
Expected: 编译失败 — 接口/实现无这些方法。

- [ ] **Step 3: 实现接口**

`UmsRoleService.java` 中，import 后追加：

```java
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.vo.UmsRoleVO;
```

方法体改为：

```java
public interface UmsRoleService extends IService<UmsRole> {
    UmsRoleVO getRoleById(Long id);

    boolean updateRole(Long id, UmsRoleDTO dto);

    boolean deleteRole(Long id);

    IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status);
}
```

- [ ] **Step 4: 实现实现类（父类改 BaseBizServiceImpl）**

`UmsRoleServiceImpl.java`：
1. 将 `import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;` 替换为 `import com.wealth.platform.common.base.BaseBizServiceImpl;`
2. 新增导入 `com.wealth.platform.system.dto.UmsRoleDTO`、`com.wealth.platform.system.vo.UmsRoleVO`、`org.springframework.transaction.annotation.Transactional`
3. 类声明改为 `public class UmsRoleServiceImpl extends BaseBizServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {`
4. 在 `pageWithFilter` 方法后新增：

```java
    @Override
    public UmsRoleVO getRoleById(Long id) {
        return getVoByIdOrThrow(id, UmsRoleVO.class, "角色");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long id, UmsRoleDTO dto) {
        return updateDto(id, dto, "角色");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        return deleteWithCheck(id, "角色");
    }
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=UmsRoleServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 8: UmsResourceService 薄 CRUD（接口 + 实现 + 父类改造 + 测试）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/UmsResourceService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsResourceServiceImpl.java`
- Create: `wealth-service/src/test/java/com/wealth/platform/system/service/impl/UmsResourceServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

创建 `UmsResourceServiceImplTest.java`：

```java
package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsResourceMapper;
import com.wealth.platform.system.vo.UmsResourceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsResourceServiceImplTest {

    @Mock
    private UmsResourceMapper umsResourceMapper;

    private UmsResourceServiceImpl umsResourceService;

    private UmsResource mockResource;

    @BeforeEach
    void setUp() {
        umsResourceService = spy(new UmsResourceServiceImpl());
        ReflectionTestUtils.setField(umsResourceService, "baseMapper", umsResourceMapper);

        mockResource = new UmsResource();
        mockResource.setId(1L);
        mockResource.setName("用户管理");
        mockResource.setUrl("/api/v1/user/**");
    }

    @Test
    @DisplayName("根据ID查询资源-成功")
    void getResourceById_Found() {
        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);

        UmsResourceVO result = umsResourceService.getResourceById(1L);

        assertNotNull(result);
        assertEquals("用户管理", result.getName());
    }

    @Test
    @DisplayName("根据ID查询资源-不存在抛404")
    void getResourceById_NotFound() {
        when(umsResourceMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> umsResourceService.getResourceById(99L));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("更新资源成功")
    void updateResource_Success() {
        UmsResourceDTO dto = new UmsResourceDTO();
        dto.setName("更新后的资源");

        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);
        when(umsResourceMapper.updateById(any(UmsResource.class))).thenReturn(1);

        boolean result = umsResourceService.updateResource(1L, dto);

        assertTrue(result);
        verify(umsResourceMapper).updateById(argThat(resource ->
                resource.getId() == 1L && "更新后的资源".equals(resource.getName())
        ));
    }

    @Test
    @DisplayName("更新资源-不存在抛404")
    void updateResource_NotFound() {
        when(umsResourceMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> umsResourceService.updateResource(99L, new UmsResourceDTO()));
        verify(umsResourceMapper, never()).updateById(isA(UmsResource.class));
    }

    @Test
    @DisplayName("删除资源成功")
    void deleteResource_Success() {
        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);
        when(umsResourceMapper.deleteById(1L)).thenReturn(1);

        boolean result = umsResourceService.deleteResource(1L);

        assertTrue(result);
        verify(umsResourceMapper).deleteById(1L);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=UmsResourceServiceImplTest -DskipTests=false`
Expected: 编译失败 — 方法不存在。

- [ ] **Step 3: 实现接口**

`UmsResourceService.java`，新增导入：

```java
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.vo.UmsResourceVO;
```

方法体改为：

```java
public interface UmsResourceService extends IService<UmsResource> {
    UmsResourceVO getResourceById(Long id);

    boolean updateResource(Long id, UmsResourceDTO dto);

    boolean deleteResource(Long id);

    /** 根据资源ID列表获取对应的URL列表 */
    List<String> getUrlByResourceIds(List<Long> resourceIds);

    // 分页条件查询
    IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url);
}
```

- [ ] **Step 4: 实现实现类（父类改 BaseBizServiceImpl）**

`UmsResourceServiceImpl.java`：
1. 将 `import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;` 替换为 `import com.wealth.platform.common.base.BaseBizServiceImpl;`
2. 新增导入 `com.wealth.platform.system.dto.UmsResourceDTO`、`com.wealth.platform.system.vo.UmsResourceVO`、`org.springframework.transaction.annotation.Transactional`
3. 类声明改为 `public class UmsResourceServiceImpl extends BaseBizServiceImpl<UmsResourceMapper, UmsResource> implements UmsResourceService {`
4. 在 `getUrlByResourceIds` 方法后新增：

```java
    @Override
    public UmsResourceVO getResourceById(Long id) {
        return getVoByIdOrThrow(id, UmsResourceVO.class, "后台资源");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResource(Long id, UmsResourceDTO dto) {
        return updateDto(id, dto, "后台资源");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResource(Long id) {
        return deleteWithCheck(id, "后台资源");
    }
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=UmsResourceServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 9: UserService 薄 CRUD（接口 + 实现 + 测试）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/service/UserService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/service/impl/UserServiceImpl.java`
- Modify: `wealth-service/src/test/java/com/wealth/platform/user/service/impl/UserServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

`UserServiceImplTest.java`，新增导入：

```java
import org.mockito.ArgumentMatchers.argThat;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.vo.UserVO;
```

在类末尾追加：

```java
    @Test
    @DisplayName("根据ID查询用户-成功")
    void getUserById_Found() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("根据ID查询用户-不存在抛404")
    void getUserById_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.getUserById(99L));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("更新用户成功-密码被清空")
    void updateUser_Success() {
        UserDTO dto = new UserDTO();
        dto.setNickname("新昵称");
        dto.setPassword("newRawPassword");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = userService.updateUser(1L, dto);

        assertTrue(result);
        verify(userMapper).updateById(argThat(user ->
                user.getId() == 1L && "新昵称".equals(user.getNickname()) && user.getPassword() == null
        ));
    }

    @Test
    @DisplayName("更新用户-不存在抛404")
    void updateUser_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> userService.updateUser(99L, new UserDTO()));
        verify(userMapper, never()).updateById(isA(User.class));
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userMapper).deleteById(1L);
    }
```

（现有 import 已有 `any`、`isA`、`never`、`verify`、`assertThrows`、`assertTrue`、`assertEquals`、`assertNotNull`。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=UserServiceImplTest -DskipTests=false`
Expected: 编译失败 — 方法不存在。

- [ ] **Step 3: 实现接口**

`UserService.java`，新增导入：

```java
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.vo.UserVO;
```

方法体改为：

```java
public interface UserService extends IService<User> {

    UserVO getUserById(Long id);

    boolean updateUser(Long id, UserDTO dto);

    boolean deleteUser(Long id);

    // 新增用户（含密码加密）
    boolean createUser(User user);

    // 用户注册
    Boolean register(User user);

    // 用户登录
    LoginVO login(LoginDTO dto);

    // 统一登录（自动识别用户类型）
    LoginVO identifyLogin(LoginDTO dto);

    // 重置密码
    Boolean resetPassword(User user, String oldPassword);

    // 分页条件查询
    IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status);
}
```

- [ ] **Step 4: 实现实现类**

`UserServiceImpl.java`，新增导入：

```java
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.vo.UserVO;
```

在类内（`createUser` 之前）新增：

```java
    @Override
    public UserVO getUserById(Long id) {
        return getVoByIdOrThrow(id, UserVO.class, "用户");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long id, UserDTO dto) {
        User existing = getEntityOrThrow(id, "用户");
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setPassword(null);
        existing.setId(id);
        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        return deleteWithCheck(id, "用户");
    }
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=UserServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 10: UmsAdminService 薄 CRUD（接口 + 实现 + 测试）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/UmsAdminService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsAdminServiceImpl.java`
- Modify: `wealth-service/src/test/java/com/wealth/platform/system/service/impl/UmsAdminServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

`UmsAdminServiceImplTest.java`，新增导入：

```java
import org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mockito.never;
import org.mockito.ArgumentMatchers.isA;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.vo.UmsAdminVO;
```

在类末尾追加：

```java
    @Test
    @DisplayName("根据ID查询管理员-成功")
    void getAdminById_Found() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);

        UmsAdminVO result = adminService.getAdminById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("根据ID查询管理员-不存在抛404")
    void getAdminById_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> adminService.getAdminById(99L));

        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("更新管理员成功-密码被清空")
    void updateAdmin_Success() {
        UmsAdminDTO dto = new UmsAdminDTO();
        dto.setNickName("新昵称");
        dto.setPassword("newRawPassword");

        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);
        when(umsAdminMapper.updateById(any(UmsAdmin.class))).thenReturn(1);

        boolean result = adminService.updateAdmin(1L, dto);

        assertTrue(result);
        verify(umsAdminMapper).updateById(argThat(admin ->
                admin.getId() == 1L && "新昵称".equals(admin.getNickName()) && admin.getPassword() == null
        ));
    }

    @Test
    @DisplayName("更新管理员-不存在抛404")
    void updateAdmin_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> adminService.updateAdmin(99L, new UmsAdminDTO()));
        verify(umsAdminMapper, never()).updateById(isA(UmsAdmin.class));
    }

    @Test
    @DisplayName("删除管理员成功")
    void deleteAdmin_Success() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);
        when(umsAdminMapper.deleteById(1L)).thenReturn(1);

        boolean result = adminService.deleteAdmin(1L);

        assertTrue(result);
        verify(umsAdminMapper).deleteById(1L);
    }
```

（现有 import 已有 `any`、`verify`、`when`、`assertThrows`、`assertEquals`、`assertNotNull`、`assertTrue`。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl wealth-service -Dtest=UmsAdminServiceImplTest -DskipTests=false`
Expected: 编译失败 — 方法不存在。

- [ ] **Step 3: 实现接口**

`UmsAdminService.java`，新增导入：

```java
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.vo.UmsAdminVO;
```

方法体开头新增：

```java
public interface UmsAdminService extends IService<UmsAdmin> {
    UmsAdminVO getAdminById(Long id);

    boolean updateAdmin(Long id, UmsAdminDTO dto);

    boolean deleteAdmin(Long id);

    TokenPair login(LoginDTO dto);
    // ... 其余方法保持不变
}
```

- [ ] **Step 4: 实现实现类**

`UmsAdminServiceImpl.java`，新增导入：

```java
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.vo.UmsAdminVO;
```

在 `getActiveByUsername` 方法前新增：

```java
    @Override
    public UmsAdminVO getAdminById(Long id) {
        return getVoByIdOrThrow(id, UmsAdminVO.class, "管理员");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(Long id, UmsAdminDTO dto) {
        UmsAdmin existing = getEntityOrThrow(id, "管理员");
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setPassword(null);
        existing.setId(id);
        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdmin(Long id) {
        return deleteWithCheck(id, "管理员");
    }
```

（`BeanConvertUtil` 已 import。）

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl wealth-service -Dtest=UmsAdminServiceImplTest -DskipTests=false`
Expected: 全部 PASS。

---

## Task 11: 关联表 Service 增加 VO + EntityOrThrow getter（接口 + 实现）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/UmsAdminRoleRelationService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsAdminRoleRelationServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/UmsRoleResourceRelationService.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsRoleResourceRelationServiceImpl.java`

> 说明：这两个 Service 已继承 `BaseBizServiceImpl`，无需测试类（getter 逻辑由 `BaseBizServiceImplTest` 覆盖；Controller 改动在 Task 14 编译验证）。

- [ ] **Step 1: 实现 UmsAdminRoleRelationService 接口**

`UmsAdminRoleRelationService.java`，新增导入：

```java
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
```

方法体开头新增：

```java
public interface UmsAdminRoleRelationService extends IService<UmsAdminRoleRelation> {
    UmsAdminRoleRelationVO getAdminRoleRelationById(Long id);

    UmsAdminRoleRelation getAdminRoleRelationEntityOrThrow(Long id);

    // 根据管理员id获取所有角色id
    List<Long> getRoleIdByAdminId(Long adminId);

    // 根据角色id获取所有管理员id（用于缓存失效）
    List<Long> getAdminIdByRoleId(Long roleId);

    // 分页条件查询
    IPage<UmsAdminRoleRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long adminId);
}
```

- [ ] **Step 2: 实现 UmsAdminRoleRelationServiceImpl**

`UmsAdminRoleRelationServiceImpl.java`，新增导入：

```java
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
```

在 `pageWithFilter` 方法前新增：

```java
    @Override
    public UmsAdminRoleRelationVO getAdminRoleRelationById(Long id) {
        return getVoByIdOrThrow(id, UmsAdminRoleRelationVO.class, "管理员角色关联");
    }

    @Override
    public UmsAdminRoleRelation getAdminRoleRelationEntityOrThrow(Long id) {
        return getEntityOrThrow(id, "管理员角色关联");
    }
```

- [ ] **Step 3: 实现 UmsRoleResourceRelationService 接口**

`UmsRoleResourceRelationService.java`，新增导入：

```java
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
```

方法体开头新增：

```java
public interface UmsRoleResourceRelationService extends IService<UmsRoleResourceRelation> {
    UmsRoleResourceRelationVO getRoleResourceRelationById(Long id);

    UmsRoleResourceRelation getRoleResourceRelationEntityOrThrow(Long id);

    // 根据角色id列表，获取所有资源id
    List<Long> getResourceIdByRoleIds(List<Long> roleIds);

    // 分页条件查询
    IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId);
}
```

- [ ] **Step 4: 实现 UmsRoleResourceRelationServiceImpl**

`UmsRoleResourceRelationServiceImpl.java`，新增导入：

```java
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
```

在 `pageWithFilter` 方法前新增：

```java
    @Override
    public UmsRoleResourceRelationVO getRoleResourceRelationById(Long id) {
        return getVoByIdOrThrow(id, UmsRoleResourceRelationVO.class, "角色资源关联");
    }

    @Override
    public UmsRoleResourceRelation getRoleResourceRelationEntityOrThrow(Long id) {
        return getEntityOrThrow(id, "角色资源关联");
    }
```

- [ ] **Step 5: 编译验证**

Run: `mvn compile -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS。

---

## Task 12: Category B Controller 瘦身（6 个）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/controller/ProductController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/controller/MarketDataController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/controller/NewsController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/message/controller/MessageController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/trade/controller/TradeOrderController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/controller/UserFavoriteController.java`

> 说明：纯路由瘦身，无测试变更。逐文件修改后统一编译验证。

- [ ] **Step 1: ProductController**

`getById`（第 41-49 行）替换为：

```java
    @Operation(summary = "根据ID查询产品")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 2: MarketDataController**

`getById`（第 49-57 行）替换为：

```java
    @Operation(summary = "根据ID查询行情数据")
    @GetMapping("/{id}")
    public Result<MarketDataVO> getById(@PathVariable Long id) {
        return Result.success(marketDataService.getMarketDataById(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 3: NewsController**

`getById`（第 41-49 行）、`update`（第 79-89 行）、`delete`（第 91-101 行）替换为：

```java
    @Operation(summary = "根据ID查询财经资讯公告")
    @GetMapping("/{id}")
    public Result<NewsVO> getById(@PathVariable Long id) {
        return Result.success(newsService.getNewsById(id));
    }
```

```java
    @Operation(summary = "更新财经资讯公告信息")
    @PutMapping("/{id}")
    @AuditLog(module = "资讯管理", operation = "更新资讯")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody NewsDTO dto) {
        return Result.success(newsService.updateNews(id, dto));
    }
```

```java
    @Operation(summary = "删除财经资讯公告（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "资讯管理", operation = "删除资讯")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(newsService.deleteNews(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 4: MessageController**

`getById`（第 42-50 行）、`update`（第 81-91 行）、`markAsRead`（第 93-101 行）、`delete`（第 113-123 行）替换为：

```java
    @Operation(summary = "根据ID查询站内消息推送信息")
    @GetMapping("/{id}")
    public Result<MessageVO> getById(@PathVariable Long id) {
        return Result.success(messageService.getMessageById(id));
    }
```

```java
    @Operation(summary = "更新站内消息推送信息")
    @PutMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "更新消息")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody MessageDTO dto) {
        return Result.success(messageService.updateMessage(id, dto));
    }
```

```java
    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(messageService.markAsRead(id));
    }
```

```java
    @Operation(summary = "删除站内消息推送（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "消息管理", operation = "删除消息")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(messageService.deleteMessage(id));
    }
```

> 保留 `import com.wealth.common.result.ResultCode;`（`batchMarkAsRead` 使用 `Result.error(ResultCode.PARAM_ERROR)`）。

- [ ] **Step 5: TradeOrderController**

`getById`（第 42-50 行）替换为：

```java
    @Operation(summary = "根据ID查询交易委托单")
    @GetMapping("/{id}")
    public Result<TradeOrderVO> getById(@PathVariable Long id) {
        return Result.success(tradeOrderService.getOrderById(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 6: UserFavoriteController**

`getById`（第 44-52 行）替换为：

```java
    @Operation(summary = "根据ID查询用户自选关注信息")
    @GetMapping("/{id}")
    public Result<UserFavoriteVO> getById(@PathVariable Long id) {
        return Result.success(userFavoriteService.getFavoriteById(id));
    }
```

> 保留 `import com.wealth.common.result.ResultCode;`（`create` 使用 `Result.error(ResultCode.FAIL.getCode(), ...)`）。

- [ ] **Step 7: 编译验证**

Run: `mvn compile -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS。

---

## Task 13: Category A Controller 瘦身（4 个）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/UmsAdminController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/controller/UserController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/UmsRoleController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/UmsResourceController.java`

- [ ] **Step 1: UmsAdminController**

`getById`（第 73-81 行）、`update`（第 112-125 行）、`delete`（第 127-137 行）替换为：

```java
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public Result<UmsAdminVO> getById(@PathVariable Long id) {
        return Result.success(umsAdminService.getAdminById(id));
    }
```

```java
    @PutMapping("/{id}")
    @Operation(summary = "修改管理员")
    @AuditLog(module = "系统管理", operation = "修改管理员")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsAdminDTO dto) {
        return Result.success(umsAdminService.updateAdmin(id, dto));
    }
```

```java
    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    @AuditLog(module = "系统管理", operation = "删除管理员")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsAdminService.deleteAdmin(id));
    }
```

删除：
- 未使用导入 `import com.wealth.common.result.ResultCode;`
- 类末尾私有方法 `clearPasswordForUpdate`（第 162-165 行）

> 保留 `import com.wealth.common.utils.BeanConvertUtil;`（`create` 使用）。

- [ ] **Step 2: UserController**

`getById`（第 53-61 行）、`update`（第 92-106 行）、`delete`（第 113-122 行）替换为：

```java
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }
```

```java
    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    @AuditLog(module = "用户管理", operation = "修改用户")
    public Result<Boolean> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto) {
        return Result.success(userService.updateUser(id, dto));
    }
```

```java
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @AuditLog(module = "用户管理", operation = "删除用户")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }
```

删除：
- 未使用导入 `import com.wealth.common.result.ResultCode;`
- 类中私有方法 `clearPasswordForUpdate`（第 108-111 行）

> 保留 `import com.wealth.common.utils.BeanConvertUtil;`（`create` 使用）。

- [ ] **Step 3: UmsRoleController**

`getById`（第 42-50 行）、`update`（第 82-94 行）、`delete`（第 96-106 行）替换为：

```java
    @Operation(summary = "根据ID查询角色信息")
    @GetMapping("/{id}")
    public Result<UmsRoleVO> getById(@PathVariable Long id) {
        return Result.success(umsRoleService.getRoleById(id));
    }
```

```java
    @Operation(summary = "更新角色信息")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新角色")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsRoleDTO dto) {
        return Result.success(umsRoleService.updateRole(id, dto));
    }
```

```java
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除角色")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsRoleService.deleteRole(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 4: UmsResourceController**

`getById`（第 45-53 行）、`update`（第 85-97 行）、`delete`（第 99-110 行）替换为：

```java
    @Operation(summary = "根据ID查询后台资源信息")
    @GetMapping("/{id}")
    public Result<UmsResourceVO> getById(@PathVariable Long id) {
        return Result.success(umsResourceService.getResourceById(id));
    }
```

```java
    @Operation(summary = "更新后台资源信息")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新资源")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsResourceDTO dto) {
        return Result.success(umsResourceService.updateResource(id, dto));
    }
```

```java
    @Operation(summary = "删除后台资源")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除资源")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsResourceService.deleteResource(id));
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 5: 编译验证**

Run: `mvn compile -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS。

---

## Task 14: 关联表 Controller 瘦身（2 个）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/UmsAdminRoleRelationController.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/controller/UmsRoleResourceRelationController.java`

> 说明：保留权限缓存清理逻辑，仅将空判改为 `getXxxEntityOrThrow`。

- [ ] **Step 1: UmsAdminRoleRelationController**

`getById`（第 44-52 行）、`update`（第 85-103 行）、`delete`（第 105-118 行）替换为：

```java
    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<UmsAdminRoleRelationVO> getById(@PathVariable Long id) {
        return Result.success(umsAdminRoleRelationService.getAdminRoleRelationById(id));
    }
```

```java
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新管理员-角色关联")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation existing = umsAdminRoleRelationService.getAdminRoleRelationEntityOrThrow(id);
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = umsAdminRoleRelationService.updateById(existing);
        // 清除新旧管理员的权限缓存
        umsAdminService.clearPermissionCache(existing.getAdminId());
        if (dto.getAdminId() != null && !dto.getAdminId().equals(existing.getAdminId())) {
            umsAdminService.clearPermissionCache(dto.getAdminId());
        }
        return Result.success(updated);
    }
```

```java
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除管理员-角色关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsAdminRoleRelation existing = umsAdminRoleRelationService.getAdminRoleRelationEntityOrThrow(id);
        boolean removed = umsAdminRoleRelationService.removeById(id);
        // 清除该管理员的权限缓存，使角色移除立即生效
        umsAdminService.clearPermissionCache(existing.getAdminId());
        return Result.success(removed);
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

- [ ] **Step 2: UmsRoleResourceRelationController**

`getById`（第 46-54 行）、`update`（第 86-104 行）、`delete`（第 106-118 行）替换为：

```java
    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<UmsRoleResourceRelationVO> getById(@PathVariable Long id) {
        return Result.success(umsRoleResourceRelationService.getRoleResourceRelationById(id));
    }
```

```java
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新角色-资源关联")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation existing = umsRoleResourceRelationService.getRoleResourceRelationEntityOrThrow(id);
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = umsRoleResourceRelationService.updateById(existing);
        // 清除新旧角色的权限缓存
        clearCacheByRoleId(existing.getRoleId());
        if (dto.getRoleId() != null && !dto.getRoleId().equals(existing.getRoleId())) {
            clearCacheByRoleId(dto.getRoleId());
        }
        return Result.success(updated);
    }
```

```java
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除角色-资源关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsRoleResourceRelation existing = umsRoleResourceRelationService.getRoleResourceRelationEntityOrThrow(id);
        boolean removed = umsRoleResourceRelationService.removeById(id);
        clearCacheByRoleId(existing.getRoleId());
        return Result.success(removed);
    }
```

删除未使用导入 `import com.wealth.common.result.ResultCode;`。

> 保留私有方法 `clearCacheByRoleId`（第 120-127 行）。

- [ ] **Step 3: 编译验证**

Run: `mvn compile -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS。

---

## Task 15: 全量验证

**Files:** 无

- [ ] **Step 1: 全量单元测试**

Run: `mvn test -pl wealth-service -DskipTests=false`
Expected: 全部 PASS（含既有测试与新增/修改测试）。

- [ ] **Step 2: 全量编译**

Run: `mvn clean install -pl wealth-common -DskipTests`，随后 `mvn clean install -DskipTests`
Expected: BUILD SUCCESS。

- [ ] **Step 3: 代码扫描清单自查**

- [ ] 无通配符导入；导入顺序符合 CODE-STANDARDS §1
- [ ] 无魔法值：`getVoByIdOrThrow` / `getEntityOrThrow` / `updateDto` / `deleteWithCheck` 中 `404` 与既有 `updateDto`/`deleteWithCheck` 一致（同风格）
- [ ] 所有 404 均走 `ServiceException`，Controller 无 `Result.error(ResultCode.NOT_FOUND)`
- [ ] 全库 grep `Result.error(ResultCode.NOT_FOUND)` 应为 0 处
- [ ] 写操作均带 `@Transactional(rollbackFor = Exception.class)`
- [ ] `UmsRoleServiceImpl` / `UmsResourceServiceImpl` 父类已改为 `BaseBizServiceImpl`

- [ ] **Step 4: 汇总改动供用户确认提交**

运行 `git status` / `git diff --stat` 汇总，将结果与改动说明提交给用户，等待用户明确指令后再 commit。

---

## 自查（Self-Review）

**Spec 覆盖：** 每个 spec 小节均有对应 Task：
- §1 BaseBizServiceImpl 模板 → Task 1
- §2 Category B getter 抛 404 + markAsRead → Task 2-6
- §3 Category A 薄 CRUD → Task 7-10
- §4 关联表 Service getter → Task 11；Controller 保留缓存逻辑 → Task 14
- §5 Controller 瘦身 → Task 12-13
- §6 测试 → Task 1-10 内联；全量验证 → Task 15

**类型一致性：** 方法名统一 — `getXxxById`（VO，抛 404）、`getXxxEntityOrThrow`（实体，抛 404）、`updateXxx` / `deleteXxx`（boolean，抛 404）。Task 7-11 接口与 Task 13-14 Controller 调用签名一致。

**占位符扫描：** 无 TBD/TODO；所有代码步骤含完整可复制代码。
