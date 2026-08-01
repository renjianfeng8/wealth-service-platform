package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.platform.product.dto.ProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.mapper.ProductMapper;
import com.wealth.platform.product.vo.ProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import com.wealth.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    private WeaProduct mockProduct;

    @BeforeEach
    void setUp() {
        productService = spy(new ProductServiceImpl());
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);

        mockProduct = new WeaProduct();
        mockProduct.setId(1L);
        mockProduct.setProductName("测试产品");
        mockProduct.setProductCode("P001");
        mockProduct.setProductType(1);
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setSort(1);
    }

    @Test
    @DisplayName("根据ID查询产品-成功")
    void getProductById_Found() {
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        ProductVO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("测试产品", result.getProductName());
        assertEquals("P001", result.getProductCode());
    }

    @Test
    @DisplayName("根据ID查询产品-不存在抛404")
    void getProductById_NotFound() {
        when(productMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                productService.getProductById(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    @DisplayName("查询产品列表-有数据")
    void getProductList_WithData() {
        WeaProduct p2 = new WeaProduct();
        p2.setId(2L);
        p2.setProductName("测试产品2");
        p2.setProductCode("P002");

        Page<WeaProduct> mockPage = new Page<>(1, 1000, 2);
        mockPage.setRecords(List.of(mockProduct, p2));
        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<ProductVO> result = productService.getProductList(1, 10);

        assertEquals(2, result.size());
        assertEquals("测试产品", result.get(0).getProductName());
        assertEquals("测试产品2", result.get(1).getProductName());
    }

    @Test
    @DisplayName("查询产品列表-空数据")
    void getProductList_Empty() {
        Page<WeaProduct> mockPage = new Page<>(1, 1000, 0);
        mockPage.setRecords(List.of());
        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        List<ProductVO> result = productService.getProductList(1, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建产品成功")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void createProduct_Success() {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("新产品");
        dto.setProductCode("P003");
        dto.setPrice(new BigDecimal("200.00"));

        when(productMapper.selectCount(any())).thenReturn(0L);

        doReturn(1).when(productMapper).insert(any(WeaProduct.class));

        boolean result = productService.createProduct(dto);

        assertTrue(result);
        verify(productMapper).insert(argThat((WeaProduct product) ->
                "新产品".equals(product.getProductName()) &&
                "P003".equals(product.getProductCode())
        ));
    }

    @Test
    @DisplayName("分页查询产品-指定类型")
    void pageProducts_WithType() {
        Page<WeaProduct> page = new Page<>(1, 10);
        Page<WeaProduct> mockPage = new Page<>(1, 10, 1);
        mockPage.setRecords(List.of(mockProduct));

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<ProductVO> result = productService.pageProducts(page, null, null, 1, null, null);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试产品", result.getRecords().get(0).getProductName());
    }

    @Test
    @DisplayName("分页查询产品-无类型筛选")
    void pageProducts_WithoutType() {
        Page<WeaProduct> page = new Page<>(1, 10);
        Page<WeaProduct> mockPage = new Page<>(1, 10, 2);
        mockPage.setRecords(List.of(mockProduct));

        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        IPage<ProductVO> result = productService.pageProducts(page, null, null, null, null, null);

        assertEquals(2, result.getTotal());
        verify(productMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新产品成功")
    void updateProduct_Success() {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("更新后的产品");

        when(productMapper.selectById(1L)).thenReturn(mockProduct);
        when(productMapper.updateById(any(WeaProduct.class))).thenReturn(1);

        boolean result = productService.updateProduct(1L, dto);

        assertTrue(result);
        verify(productMapper).updateById(argThat((WeaProduct product) ->
                "更新后的产品".equals(product.getProductName()) &&
                product.getId() == 1L
        ));
    }

    @Test
    @DisplayName("更新产品-不存在抛404")
    void updateProduct_NotFound() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertThrows(ServiceException.class, () ->
                productService.updateProduct(99L, new ProductDTO()));
        verify(productMapper, never()).updateById(isA(WeaProduct.class));
    }

    @Test
    @DisplayName("删除产品成功")
    void deleteProduct_Success() {
        when(productMapper.selectById(1L)).thenReturn(mockProduct);
        when(productMapper.deleteById(1L)).thenReturn(1);

        boolean result = productService.deleteProduct(1L);

        assertTrue(result);
        verify(productMapper).deleteById(1L);
    }
}
