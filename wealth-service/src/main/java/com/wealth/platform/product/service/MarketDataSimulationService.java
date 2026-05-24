package com.wealth.platform.product.service;

import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.FinMarketDataMapper;
import com.wealth.platform.product.vo.FinMarketDataVO;
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
import java.util.List;
import java.util.Random;

/**
 * 行情模拟推演服务，定时模拟价格波动并广播给所有 SSE 客户端。
 */
@Slf4j
@Service
public class MarketDataSimulationService {

    private final FinMarketDataMapper marketDataMapper;
    private final MarketDataPushService pushService;

    /** 自注入代理，解决 @Transactional 自调用失效问题 */
    @Lazy
    @Autowired
    private MarketDataSimulationService self;

    private volatile List<WeaMarketData> cachedMarketData;
    private final Random random = new Random();

    public MarketDataSimulationService(FinMarketDataMapper marketDataMapper,
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
        cachedMarketData = marketDataMapper.selectList(null);
    }

    /**
     * 每 2 秒模拟一次行情变化：高斯随机游走，更新数据库并广播。
     * fixedDelay 保证上次执行完成后再开始下一次，避免重叠执行引发并发问题。
     */
    @Scheduled(fixedDelayString = "${market.simulation.interval:2000}")
    public void simulateMarketTick() {
        if (cachedMarketData.isEmpty()) return;

        // 1. 事务内更新数据库（通过代理调用以触发 @Transactional）
        self.simulateTickDb();

        // 2. 事务外广播 SSE（避免广播异常导致 DB 回滚）
        List<FinMarketDataVO> voList = BeanConvertUtil.convertList(cachedMarketData, FinMarketDataVO.class);
        try {
            pushService.broadcastMarketUpdate(voList);
        } catch (Exception e) {
            log.error("行情广播失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void simulateTickDb() {
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
    }

    /** 返回当前缓存的全部行情数据（全量快照） */
    public List<FinMarketDataVO> getAllMarketData() {
        return BeanConvertUtil.convertList(cachedMarketData, FinMarketDataVO.class);
    }
}
