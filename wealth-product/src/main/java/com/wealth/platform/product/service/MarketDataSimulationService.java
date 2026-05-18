package com.wealth.platform.product.service;

import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.FinMarketDataMapper;
import com.wealth.platform.product.vo.FinMarketDataVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 行情模拟推演服务，定时模拟价格波动并广播给所有 SSE 客户端。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataSimulationService {

    private final FinMarketDataMapper marketDataMapper;
    private final MarketDataPushService pushService;

    private List<WeaMarketData> cachedMarketData;
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        loadMarketData();
        log.info("行情模拟服务初始化完成，加载 {} 条产品行情", cachedMarketData.size());
    }

    private void loadMarketData() {
        cachedMarketData = marketDataMapper.selectList(null);
    }

    /**
     * 每 2 秒模拟一次行情变化：高斯随机游走，更新数据库并广播。
     */
    @Scheduled(fixedRate = 2000)
    @Transactional(rollbackFor = Exception.class)
    public void simulateMarketTick() {
        if (cachedMarketData.isEmpty()) return;

        for (WeaMarketData data : cachedMarketData) {
            BigDecimal oldPrice = data.getCurrentPrice();
            // 高斯随机游走，波动幅度约 0.2%
            double changeFactor = random.nextGaussian() * 0.002;
            BigDecimal newPrice = oldPrice.multiply(BigDecimal.valueOf(1 + changeFactor))
                    .setScale(2, RoundingMode.HALF_UP);

            // 确保价格不低于 0.01
            if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                newPrice = BigDecimal.valueOf(0.01);
            }

            data.setCurrentPrice(newPrice);
            data.setRiseFall(newPrice.subtract(data.getClosePrice()));
            data.setRiseFallRate(data.getClosePrice().compareTo(BigDecimal.ZERO) > 0
                    ? data.getRiseFall().divide(data.getClosePrice(), 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            data.setMarketTime(LocalDateTime.now());
            data.setHighestPrice(newPrice.compareTo(data.getHighestPrice()) > 0 ? newPrice : data.getHighestPrice());
            data.setLowestPrice(newPrice.compareTo(data.getLowestPrice()) < 0 ? newPrice : data.getLowestPrice());

            marketDataMapper.updateById(data);
        }

        // 广播完整行情快照
        List<FinMarketDataVO> voList = BeanConvertUtil.convertList(cachedMarketData, FinMarketDataVO.class);
        pushService.broadcastMarketUpdate(voList);
    }

    /** 返回当前缓存的全部行情数据（全量快照） */
    public List<FinMarketDataVO> getAllMarketData() {
        return BeanConvertUtil.convertList(cachedMarketData, FinMarketDataVO.class);
    }
}
