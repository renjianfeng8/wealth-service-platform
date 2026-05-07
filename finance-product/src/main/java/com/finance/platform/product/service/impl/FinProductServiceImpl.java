package com.finance.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.product.dto.FinProductDTO;
import com.finance.platform.product.entity.FinProduct;
import com.finance.platform.product.mapper.FinProductMapper;
import com.finance.platform.product.service.FinProductService;
import com.finance.platform.product.vo.FinProductVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinProductServiceImpl extends ServiceImpl<FinProductMapper, FinProduct>
        implements FinProductService {

    @Override
    public FinProductVO getProductById(Long id) {
        FinProduct entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinProductVO vo = new FinProductVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinProductVO> getProductList() {
        List<FinProduct> list = list();
        return list.stream().map(entity -> {
            FinProductVO vo = new FinProductVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createProduct(FinProductDTO dto) {
        FinProduct entity = new FinProduct();
        BeanUtils.copyProperties(dto, entity);
        return save(entity);
    }

    @Override
    public boolean updateProduct(Long id, FinProductDTO dto) {
        FinProduct entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    public boolean deleteProduct(Long id) {
        return removeById(id);
    }
}