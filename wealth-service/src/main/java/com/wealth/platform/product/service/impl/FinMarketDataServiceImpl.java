package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.platform.product.dto.FinMarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.FinMarketDataMapper;
import com.wealth.platform.product.service.FinMarketDataService;
import com.wealth.platform.product.vo.FinMarketDataVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.wealth.common.utils.BeanConvertUtil;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinMarketDataServiceImpl extends ServiceImpl<FinMarketDataMapper, WeaMarketData>
        implements FinMarketDataService {

    @Override
    public FinMarketDataVO getMarketDataById(Long id) {
        WeaMarketData entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinMarketDataVO vo = BeanConvertUtil.convert(entity, FinMarketDataVO.class);
        return vo;
    }

    @Override
    public List<FinMarketDataVO> getMarketDataList() {
        List<WeaMarketData> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> {
            FinMarketDataVO vo = BeanConvertUtil.convert(entity, FinMarketDataVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMarketData(FinMarketDataDTO dto) {
        WeaMarketData entity = BeanConvertUtil.convert(dto, WeaMarketData.class);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMarketData(Long id, FinMarketDataDTO dto) {
        WeaMarketData entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMarketData(Long id) {
        return removeById(id);
    }

    @Override
    public IPage<WeaMarketData> pageWithFilter(Integer pageNum, Integer pageSize, String productCode) {
        Page<WeaMarketData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WeaMarketData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(productCode)) {
            wrapper.like(WeaMarketData::getProductCode, productCode);
        }
        wrapper.orderByDesc(WeaMarketData::getMarketTime);
        return baseMapper.selectPage(page, wrapper);
    }
}