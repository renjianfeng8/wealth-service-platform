package com.wealth.platform.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.contract.DashboardMarketDataProvider;
import com.wealth.common.dto.DashboardMarketDataDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.product.dto.MarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.MarketDataMapper;
import com.wealth.platform.product.service.MarketDataService;
import com.wealth.platform.product.vo.MarketDataVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.wealth.common.utils.BeanConvertUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketDataServiceImpl extends ServiceImpl<MarketDataMapper, WeaMarketData>
        implements MarketDataService, DashboardMarketDataProvider {

    @Override
    public MarketDataVO getMarketDataById(Long id) {
        WeaMarketData entity = getById(id);
        if (entity == null) {
            return null;
        }
        MarketDataVO vo = BeanConvertUtil.convert(entity, MarketDataVO.class);
        return vo;
    }

    @Override
    public List<MarketDataVO> getMarketDataList() {
        List<WeaMarketData> list = page(new Page<>(1, 1000), new LambdaQueryWrapper<>()).getRecords();
        return list.stream().map(entity -> {
            MarketDataVO vo = BeanConvertUtil.convert(entity, MarketDataVO.class);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMarketData(MarketDataDTO dto) {
        WeaMarketData entity = BeanConvertUtil.convert(dto, WeaMarketData.class);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMarketData(Long id, MarketDataDTO dto) {
        WeaMarketData entity = getById(id);
        if (entity == null) {
            throw new ServiceException(404, "market data not found");
        }
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMarketData(Long id) {
        if (getById(id) == null) {
            throw new ServiceException(404, "market data not found");
        }
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

    @Override
    public BigDecimal sumPrice() {
        return baseMapper.sumPrice();
    }

    @Override
    public List<BigDecimal> findLatestTwoPrices() {
        return baseMapper.findLatestTwoPrices();
    }

    @Override
    public List<DashboardMarketDataDTO> findCandles(String productCode, LocalDateTime startTime, LocalDateTime endTime) {
        List<WeaMarketData> records = baseMapper.selectList(
                new LambdaQueryWrapper<WeaMarketData>()
                        .eq(WeaMarketData::getProductCode, productCode)
                        .ge(WeaMarketData::getMarketTime, startTime)
                        .le(WeaMarketData::getMarketTime, endTime)
                        .orderByAsc(WeaMarketData::getMarketTime)
        );
        return BeanConvertUtil.convertList(records, DashboardMarketDataDTO.class);
    }
}
