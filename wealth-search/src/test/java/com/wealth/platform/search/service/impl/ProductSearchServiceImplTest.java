package com.wealth.platform.search.service.impl;

import com.wealth.platform.search.entity.ProductDocument;
import com.wealth.platform.search.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductSearchServiceImpl searchService;

    private ProductDocument mockDoc;

    @BeforeEach
    void setUp() {
        mockDoc = new ProductDocument();
        mockDoc.setId(1L);
        mockDoc.setProductName("测试基金");
        mockDoc.setProductCode("FUND001");
        mockDoc.setProductType(1);
        mockDoc.setPrice(new BigDecimal("1.50"));
    }

    @Test
    @DisplayName("保存文档到ES-成功")
    void save_Success() {
        when(productRepository.save(any(ProductDocument.class))).thenReturn(mockDoc);

        ProductDocument result = searchService.save(mockDoc);

        assertNotNull(result);
        assertEquals("测试基金", result.getProductName());
        assertEquals("FUND001", result.getProductCode());
        verify(productRepository).save(mockDoc);
    }

    @Test
    @DisplayName("根据ID查询ES文档-成功")
    void getById_Found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockDoc));

        ProductDocument result = searchService.getById(1L);

        assertNotNull(result);
        assertEquals("测试基金", result.getProductName());
    }

    @Test
    @DisplayName("根据ID查询ES文档-不存在返回null")
    void getById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductDocument result = searchService.getById(99L);

        assertNull(result);
    }

    @Test
    @DisplayName("搜索ES文档-成功")
    void search_Success() {
        Page<ProductDocument> mockPage = new PageImpl<>(List.of(mockDoc));
        when(productRepository.searchByKeyword(anyString(), any(Pageable.class))).thenReturn(mockPage);

        Page<ProductDocument> result = searchService.search("基金", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试基金", result.getContent().get(0).getProductName());
        verify(productRepository).searchByKeyword(eq("基金"), any(Pageable.class));
    }

    @Test
    @DisplayName("搜索ES-无结果返回空页")
    void search_NoResults() {
        Page<ProductDocument> emptyPage = new PageImpl<>(List.of());
        when(productRepository.searchByKeyword(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        Page<ProductDocument> result = searchService.search("不存在", 1, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("从ES删除文档-成功")
    void deleteById_Success() {
        doNothing().when(productRepository).deleteById(1L);

        searchService.deleteById(1L);

        verify(productRepository).deleteById(1L);
    }
}
