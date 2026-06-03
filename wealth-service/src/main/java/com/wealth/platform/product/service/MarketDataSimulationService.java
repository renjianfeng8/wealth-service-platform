package com.wealth.platform.product.service;

import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.MarketDataMapper;
import com.wealth.platform.product.vo.MarketDataVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 行情模拟推演服务，定时模拟价格波动并广播给所有 SSE 客户端。
 */
@Slf4j
@Service
public class MarketDataSimulationService {

    private final MarketDataMapper marketDataMapper;
    private final MarketDataPushService pushService;

    /** 自注入代理，解决 @Transactional 自调用失效问题 */
    @Lazy
    @Autowired
    private MarketDataSimulationService self;

    private volatile List<WeaMarketData> cachedMarketData;
    private final Random random = new Random();

    public MarketDataSimulationService(MarketDataMapper marketDataMapper,
                                        MarketDataPushService pushService) {
        this.marketDataMapper = marketDataMapper;
        this.pushService = pushService;
    }

    @PostConstruct
    public void init() {
        loadMarketData();
        log.info("行情模拟服务初始化完成，加载 {} 条产品行情", cachedMarketData.size());
    }

    private void loadMarketData() {
        List<WeaMarketData> records = marketDataMapper.selectList(null);
        cachedMarketData = records != null ? records : Collections.emptyList();
    }

    /**
     * 每 2 秒模拟一次行情变化：高斯随机游走，更新数据库并广播。
     * fixedDelay 保证上次执行完成后再开始下一次，避免重叠执行引发并发问题。
     */
    @Scheduled(fixedDelayString = "${market.simulation.interval:2000}")
    public void simulateMarketTick() {
        if (cachedMarketData == null) {
            loadMarketData();
        }
        if (cachedMarketData.isEmpty()) return;

        // 1. 事务内更新数据库（通过代理调用以触发 @Transactional）
        self.simulateTickDb();

        // 2. 事务外广播 SSE（避免广播异常导致 DB 回滚）
        List<MarketDataVO> voList = BeanConvertUtil.convertList(cachedMarketData, MarketDataVO.class);
        try {
            pushService.broadcastMarketUpdate(voList);
        } catch (Exception e) {
            log.error("行情广播失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void simulateTickDb() {
        if (cachedMarketData == null || cachedMarketData.isEmpty()) {
            return;
        }
        for (WeaMarketData data : cachedMarketData) {
            if (data == null) {
                continue;
            }
            BigDecimal oldPrice = defaultPrice(data.getCurrentPrice(), data.getClosePrice());
            BigDecimal closePrice = defaultPrice(data.getClosePrice(), oldPrice);
            BigDecimal highestPrice = defaultPrice(data.getHighestPrice(), oldPrice);
            BigDecimal lowestPrice = defaultPrice(data.getLowestPrice(), oldPrice);

            // 高斯随机游走，波动幅度约 0.2%
            double changeFactor = random.nextGaussian() * 0.002;
            BigDecimal newPrice = oldPrice.multiply(BigDecimal.valueOf(1 + changeFactor))
                    .setScale(2, RoundingMode.HALF_UP);

            // 确保价格不低于 0.01
            if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                newPrice = BigDecimal.valueOf(0.01);
            }

            data.setCurrentPrice(newPrice);
            data.setRiseFall(newPrice.subtract(closePrice));
            data.setRiseFallRate(closePrice.compareTo(BigDecimal.ZERO) > 0
                    ? data.getRiseFall().divide(closePrice, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            data.setMarketTime(LocalDateTime.now());
            data.setHighestPrice(newPrice.compareTo(highestPrice) > 0 ? newPrice : highestPrice);
            data.setLowestPrice(newPrice.compareTo(lowestPrice) < 0 ? newPrice : lowestPrice);

            marketDataMapper.updateById(data);
        }
    }

    /** 返回当前缓存的全部行情数据（全量快照） */
    public List<MarketDataVO> getAllMarketData() {
        if (cachedMarketData == null) {
            loadMarketData();
        }
        return BeanConvertUtil.convertList(cachedMarketData, MarketDataVO.class);
    }

    private BigDecimal defaultPrice(BigDecimal value, BigDecimal fallback) {
        if (value != null) {
            return value;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }
}
