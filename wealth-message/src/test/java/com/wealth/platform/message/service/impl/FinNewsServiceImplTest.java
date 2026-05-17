package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.message.dto.FinNewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.mapper.FinNewsMapper;
import com.wealth.platform.message.vo.FinNewsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FinNewsServiceImplTest {

    @Mock
    private FinNewsMapper newsMapper;

    private FinNewsServiceImpl newsService;

    private WeaNews mockNews;

    @BeforeEach
    void setUp() {
        newsService = new FinNewsServiceImpl();
        ReflectionTestUtils.setField(newsService, "baseMapper", newsMapper);

        mockNews = new WeaNews();
        mockNews.setId(1L);
        mockNews.setTitle("测试资讯标题");
        mockNews.setContent("测试资讯内容");
        mockNews.setNewsType(1);
        mockNews.setSource("测试来源");
    }

    @Test
    @DisplayName("根据ID查询资讯-成功")
    void getNewsById_Found() {
        when(newsMapper.selectById(1L)).thenReturn(mockNews);

        FinNewsVO result = newsService.getNewsById(1L);

        assertNotNull(result);
        assertEquals("测试资讯标题", result.getTitle());
        assertEquals(1, result.getNewsType());
    }

    @Test
    @DisplayName("根据ID查询资讯-不存在返回null")
    void getNewsById_NotFound() {
        when(newsMapper.selectById(99L)).thenReturn(null);

        FinNewsVO result = newsService.getNewsById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询资讯列表-有数据")
    void getNewsList_WithData() {
        when(newsMapper.selectList(any())).thenReturn(List.of(mockNews));

        List<FinNewsVO> result = newsService.getNewsList();

        assertEquals(1, result.size());
        assertEquals("测试资讯标题", result.get(0).getTitle());
    }

    @Test
    @DisplayName("查询资讯列表-空数据")
    void getNewsList_Empty() {
        when(newsMapper.selectList(any())).thenReturn(List.of());

        List<FinNewsVO> result = newsService.getNewsList();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建资讯成功")
    void createNews_Success() {
        FinNewsDTO dto = new FinNewsDTO();
        dto.setTitle("新资讯");
        dto.setContent("新资讯内容");
        dto.setNewsType(2);

        doReturn(1).when(newsMapper).insert(any(WeaNews.class));

        boolean result = newsService.createNews(dto);

        assertTrue(result);
        verify(newsMapper).insert(argThat((WeaNews news) ->
                "新资讯".equals(news.getTitle()) &&
                "新资讯内容".equals(news.getContent()) &&
                2 == news.getNewsType()
        ));
    }

    @Test
    @DisplayName("分页查询资讯-指定类型")
    void pageNews_WithType() {
        Page<WeaNews> page = new Page<>(1, 10);
        Page<WeaNews> mockPage = new Page<>(1, 10, 1);
        mockPage.setRecords(List.of(mockNews));

        when(newsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<FinNewsVO> result = newsService.pageNews(page, 1);

        assertEquals(1, result.getTotal());
        assertEquals("测试资讯标题", result.getRecords().get(0).getTitle());
    }

    @Test
    @DisplayName("分页查询资讯-无类型筛选")
    void pageNews_WithoutType() {
        Page<WeaNews> page = new Page<>(1, 10);
        Page<WeaNews> mockPage = new Page<>(1, 10, 5);
        mockPage.setRecords(List.of(mockNews));

        when(newsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<FinNewsVO> result = newsService.pageNews(page, null);

        assertEquals(5, result.getTotal());
    }

    @Test
    @DisplayName("更新资讯成功-null安全更新")
    void updateNews_Success() {
        FinNewsDTO dto = new FinNewsDTO();
        dto.setTitle("更新后的标题");

        when(newsMapper.selectById(1L)).thenReturn(mockNews);
        when(newsMapper.updateById(any(WeaNews.class))).thenReturn(1);

        boolean result = newsService.updateNews(1L, dto);

        assertTrue(result);
        verify(newsMapper).updateById(argThat((WeaNews news) ->
                "更新后的标题".equals(news.getTitle()) && news.getId() == 1L
        ));
    }

    @Test
    @DisplayName("更新资讯-不存在返回false")
    void updateNews_NotFound() {
        when(newsMapper.selectById(99L)).thenReturn(null);

        boolean result = newsService.updateNews(99L, new FinNewsDTO());

        assertFalse(result);
        verify(newsMapper, never()).updateById(isA(WeaNews.class));
    }

    @Test
    @DisplayName("删除资讯成功")
    void deleteNews_Success() {
        when(newsMapper.deleteById(1L)).thenReturn(1);

        boolean result = newsService.deleteNews(1L);

        assertTrue(result);
        verify(newsMapper).deleteById(1L);
    }
}
