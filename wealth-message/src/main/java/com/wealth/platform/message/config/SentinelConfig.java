package com.wealth.platform.message.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        FlowRule rule = new FlowRule();
        rule.setResource("POST:/message/WeaMessage");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(200);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(List.of(rule));
    }
}
