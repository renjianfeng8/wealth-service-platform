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
