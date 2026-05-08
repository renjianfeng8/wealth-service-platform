package com.finance.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.platform.product.dto.FinMarketDataDTO;
import com.finance.platform.product.entity.FinMarketData;
import com.finance.platform.product.mapper.FinMarketDataMapper;
import com.finance.platform.product.service.FinMarketDataService;
import com.finance.platform.product.vo.FinMarketDataVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinMarketDataServiceImpl extends ServiceImpl<FinMarketDataMapper, FinMarketData>
        implements FinMarketDataService {

    @Override
    public FinMarketDataVO getMarketDataById(Long id) {
        FinMarketData entity = getById(id);
        if (entity == null) {
            return null;
        }
        FinMarketDataVO vo = new FinMarketDataVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FinMarketDataVO> getMarketDataList() {
        List<FinMarketData> list = list();
        return list.stream().map(entity -> {
            FinMarketDataVO vo = new FinMarketDataVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMarketData(FinMarketDataDTO dto) {
        FinMarketData entity = new FinMarketData();
        BeanUtils.copyProperties(dto, entity);
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMarketData(Long id, FinMarketDataDTO dto) {
        FinMarketData entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMarketData(Long id) {
        return removeById(id);
    }
}