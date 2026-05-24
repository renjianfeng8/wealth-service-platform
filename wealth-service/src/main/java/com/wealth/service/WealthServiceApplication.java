package com.wealth.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.wealth.platform",
        "com.wealth.user",
        "com.wealth.common",
        "com.wealth.service"
})
@MapperScan({
        "com.wealth.platform.system.mapper",
        "com.wealth.platform.product.mapper",
        "com.wealth.platform.trade.mapper",
        "com.wealth.platform.message.mapper",
        "com.wealth.platform.search.mapper",
        "com.wealth.user.mapper"
})
public class WealthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthServiceApplication.class, args);
    }
}
