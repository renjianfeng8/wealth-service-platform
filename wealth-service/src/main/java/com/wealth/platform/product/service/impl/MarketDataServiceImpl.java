package com.wealth.platform.product.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.contract.DashboardMarketDataProvider;
import com.wealth.common.dto.DashboardMarketDataDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.MarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.MarketDataMapper;
import com.wealth.platform.product.service.MarketDataService;
import com.wealth.platform.product.vo.MarketDataVO;

@Service
public class MarketDataServiceImpl extends BaseBizServiceImpl<MarketDataMapper, WeaMarketData>
        implements MarketDataService, DashboardMarketDataProvider {

    @Override
    public MarketDataVO getMarketDataById(Long id) {
        return getVoByIdOrThrow(id, MarketDataVO.class, "行情数据");
    }

    @Override
    public List<MarketDataVO> getMarketDataList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, MarketDataVO.class);
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
        return updateDto(id, dto, "行情数据");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMarketData(Long id) {
        return deleteWithCheck(id, "行情数据");
    }

    @Override
    public IPage<WeaMarketData> pageWithFilter(Integer pageNum, Integer pageSize, String productCode) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(WeaMarketData::getMarketTime),
                like(WeaMarketData::getProductCode, productCode));
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
                        .last("LIMIT 100000")
        );
        return BeanConvertUtil.convertList(records, DashboardMarketDataDTO.class);
    }
}
