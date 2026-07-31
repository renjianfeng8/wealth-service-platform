package com.wealth.gateway;

import com.wealth.common.utils.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JwtUtil.class)
public class WealthGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WealthGatewayApplication.class, args);
    }
}