package com.finance.platform.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.platform.product.dto.FinMarketDataDTO;
import com.finance.platform.product.entity.FinMarketData;
import com.finance.platform.product.vo.FinMarketDataVO;
import java.util.List;

/**
 * 行情数据表业务层接口。
 */
public interface FinMarketDataService extends IService<FinMarketData> {

    // 根据ID查单个VO
    FinMarketDataVO getMarketDataById(Long id);

    // 查询列表VO
    List<FinMarketDataVO> getMarketDataList();

    // 新增：接收DTO
    boolean createMarketData(FinMarketDataDTO dto);

    // 更新：接收DTO
    boolean updateMarketData(Long id, FinMarketDataDTO dto);

    // 删除
    boolean deleteMarketData(Long id);
}