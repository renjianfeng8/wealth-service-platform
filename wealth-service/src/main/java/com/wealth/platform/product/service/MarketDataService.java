package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.MarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.vo.MarketDataVO;
import java.util.List;

/**
 * 行情数据表业务层接口。
 */
public interface MarketDataService extends IService<WeaMarketData> {

    // 根据ID查单个VO
    MarketDataVO getMarketDataById(Long id);

    // 查询列表VO
    List<MarketDataVO> getMarketDataList(Integer pageNum, Integer pageSize);

    // 新增：接收DTO
    boolean createMarketData(MarketDataDTO dto);

    // 更新：接收DTO
    boolean updateMarketData(Long id, MarketDataDTO dto);

    // 删除
    boolean deleteMarketData(Long id);

    // 分页查询（带产品代码筛选）
    IPage<WeaMarketData> pageWithFilter(Integer pageNum, Integer pageSize, String productCode);
}