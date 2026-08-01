package com.wealth.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.wealth.common.result.Result;
import com.wealth.common.utils.ResultJson;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashSet;

@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initGatewayRules() {
        var rules = new HashSet<GatewayFlowRule>();
        rules.add(new GatewayFlowRule("wealth-trade").setCount(50));
        rules.add(new GatewayFlowRule("wealth-user").setCount(100));
        rules.add(new GatewayFlowRule("wealth-product").setCount(100));
        rules.add(new GatewayFlowRule("wealth-system").setCount(100));
        rules.add(new GatewayFlowRule("wealth-message").setCount(100));
        rules.add(new GatewayFlowRule("wealth-search").setCount(30));
        GatewayRuleManager.loadRules(rules);

        GatewayCallbackManager.setBlockHandler((exchange, t) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(ResultJson.write(Result.error(429, "请求过于频繁，请稍后再试"))))
        );
    }
}
