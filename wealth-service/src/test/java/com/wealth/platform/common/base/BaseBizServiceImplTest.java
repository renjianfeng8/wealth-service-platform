package com.wealth.platform.common.base;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.entity.BaseEntity;
import com.wealth.common.exception.ServiceException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static com.wealth.platform.common.base.BaseBizServiceImpl.eq;
import static com.wealth.platform.common.base.BaseBizServiceImpl.like;
import static com.wealth.platform.common.base.BaseBizServiceImpl.orderByDesc;
import static com.wealth.platform.common.base.BaseBizServiceImpl.positiveEq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseBizServiceImplTest {

    @Data
    @TableName("test_entity")
    @EqualsAndHashCode(callSuper = true)
    static class TestEntity extends BaseEntity {
        @TableField("name")
        private String name;
        @TableField("sort")
        private Integer sort;
    }

    interface TestMapper extends BaseMapper<TestEntity> {}

    static class TestServiceImpl extends BaseBizServiceImpl<TestMapper, TestEntity> {}

    @Mock
    private TestMapper testMapper;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestEntity.class);
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
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        when(testMapper.selectById(1L)).thenReturn(entity);

        BaseEntity result = testService.getEntityOrThrow(1L, "测试");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryWrapper<TestEntity> captureWrapper() {
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(testMapper).selectPage(any(Page.class), captor.capture());
        return (LambdaQueryWrapper<TestEntity>) captor.getValue();
    }

    @Test
    @DisplayName("pageWithFilter-like自动转义通配符")
    void pageWithFilter_should_escape_like_wildcards() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, like(TestEntity::getName, "50%"));

        LambdaQueryWrapper<TestEntity> w = captureWrapper();
        w.getSqlSegment();
        assertTrue(w.getParamNameValuePairs().values().stream()
                .anyMatch(v -> String.valueOf(v).contains("50\\%")));
    }

    @Test
    @DisplayName("pageWithFilter-like空白值跳过")
    void pageWithFilter_should_skip_like_when_blank() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, like(TestEntity::getName, "   "));

        LambdaQueryWrapper<TestEntity> w = captureWrapper();
        w.getSqlSegment();
        assertTrue(w.getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-eq null值跳过")
    void pageWithFilter_should_skip_eq_when_null() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, eq(TestEntity::getId, null));

        LambdaQueryWrapper<TestEntity> w = captureWrapper();
        w.getSqlSegment();
        assertTrue(w.getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-positiveEq 0值跳过")
    void pageWithFilter_should_skip_positiveEq_when_zero_or_negative() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, positiveEq(TestEntity::getId, 0L));

        LambdaQueryWrapper<TestEntity> w = captureWrapper();
        w.getSqlSegment();
        assertTrue(w.getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-应用排序")
    void pageWithFilter_should_apply_order_when_provided() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, orderByDesc(TestEntity::getCreateTime));

        String sql = captureWrapper().getSqlSegment();
        assertTrue(sql.contains("create_time"));
        assertTrue(sql.contains("DESC"));
    }

    @Test
    @DisplayName("pageWithFilter-null排序不生效")
    void pageWithFilter_should_not_order_when_order_null() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null);

        assertFalse(captureWrapper().getSqlSegment().contains("ORDER BY"));
    }
}
