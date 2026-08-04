package com.wealth.common.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 合并后的统一 Sentinel 限流规则配置。
 *
 * <p>原 trade 和 message 模块各有独立 SentinelConfig，都通过 @PostConstruct 调用
 * FlowRuleManager.loadRules()，后初始化的会覆盖前者的规则。合并后统一在此加载全部规则。
 */
@Configuration
public class UnifiedSentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        FlowRuleManager.loadRules(List.of(
                new FlowRule() {{
                    setResource("POST:/trade/wea-trade-order");
                    setGrade(RuleConstant.FLOW_GRADE_QPS);
                    setCount(100);
                    setLimitApp("default");
                }},
                new FlowRule() {{
                    setResource("POST:/message/wea-message");
                    setGrade(RuleConstant.FLOW_GRADE_QPS);
                    setCount(200);
                    setLimitApp("default");
                }}
        ));
    }
}
