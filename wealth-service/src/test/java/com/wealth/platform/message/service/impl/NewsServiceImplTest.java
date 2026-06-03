package com.wealth.platform.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.message.dto.NewsDTO;
import com.wealth.platform.message.entity.WeaNews;
import com.wealth.platform.message.mapper.NewsMapper;
import com.wealth.platform.message.vo.NewsVO;
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
class NewsServiceImplTest {

    @Mock
    private NewsMapper newsMapper;

    private NewsServiceImpl newsService;

    private WeaNews mockNews;

    @BeforeEach
    void setUp() {
        newsService = spy(new NewsServiceImpl());
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

        NewsVO result = newsService.getNewsById(1L);

        assertNotNull(result);
        assertEquals("测试资讯标题", result.getTitle());
        assertEquals(1, result.getNewsType());
    }

    @Test
    @DisplayName("根据ID查询资讯-不存在返回null")
    void getNewsById_NotFound() {
        when(newsMapper.selectById(99L)).thenReturn(null);

        NewsVO result = newsService.getNewsById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("查询资讯列表-有数据")
    void getNewsList_WithData() {
        Page<WeaNews> mockPage = new Page<>(1, 1000, 1);
        mockPage.setRecords(List.of(mockNews));
        when(newsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<NewsVO> result = newsService.getNewsList();

        assertEquals(1, result.size());
        assertEquals("测试资讯标题", result.get(0).getTitle());
    }

    @Test
    @DisplayName("查询资讯列表-空数据")
    void getNewsList_Empty() {
        Page<WeaNews> mockPage = new Page<>(1, 1000, 0);
        mockPage.setRecords(List.of());
        when(newsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<NewsVO> result = newsService.getNewsList();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建资讯成功")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void createNews_Success() {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("新资讯");
        dto.setContent("新资讯内容");
        dto.setNewsType(2);

        LambdaQueryChainWrapper<WeaNews> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.count()).thenReturn(0L);
        doReturn(qc).when(newsService).lambdaQuery();

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

        IPage<NewsVO> result = newsService.pageNews(page, null, null, 1);

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

        IPage<NewsVO> result = newsService.pageNews(page, null, null, null);

        assertEquals(5, result.getTotal());
    }

    @Test
    @DisplayName("更新资讯成功-null安全更新")
    void updateNews_Success() {
        NewsDTO dto = new NewsDTO();
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

        ServiceException exception = assertThrows(ServiceException.class, () ->
                newsService.updateNews(99L, new NewsDTO()));

        assertEquals(404, exception.getCode());
        verify(newsMapper, never()).updateById(isA(WeaNews.class));
    }

    @Test
    @DisplayName("删除资讯成功")
    void deleteNews_Success() {
        when(newsMapper.selectById(1L)).thenReturn(mockNews);
        when(newsMapper.deleteById(1L)).thenReturn(1);

        boolean result = newsService.deleteNews(1L);

        assertTrue(result);
        verify(newsMapper).deleteById(1L);
    }
}
