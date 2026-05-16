package com.wealth.platform.search.service.impl;

import com.wealth.platform.search.entity.ProductDocument;
import com.wealth.platform.search.repository.ProductRepository;
import com.wealth.platform.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;

    @Override
    public ProductDocument save(ProductDocument document) {
        return productRepository.save(document);
    }

    @Override
    public ProductDocument getById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Page<ProductDocument> search(String keyword, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return productRepository.searchByKeyword(keyword, pageRequest);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
