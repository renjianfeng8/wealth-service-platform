package com.wealth.platform.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wealth.platform.product.dto.FinMarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.vo.FinMarketDataVO;
import java.util.List;

/**
 * 琛屾儏鏁版嵁琛ㄤ笟鍔″眰鎺ュ彛銆?
 */
public interface FinMarketDataService extends IService<WeaMarketData> {

    // 鏍规嵁ID鏌ュ崟涓猇O
    FinMarketDataVO getMarketDataById(Long id);

    // 鏌ヨ鍒楄〃VO
    List<FinMarketDataVO> getMarketDataList();

    // 鏂板锛氭帴鏀禗TO
    boolean createMarketData(FinMarketDataDTO dto);

    // 鏇存柊锛氭帴鏀禗TO
    boolean updateMarketData(Long id, FinMarketDataDTO dto);

    // 鍒犻櫎
    boolean deleteMarketData(Long id);
}