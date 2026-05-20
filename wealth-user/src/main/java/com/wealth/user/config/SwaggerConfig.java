package com.wealth.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("金融系统 - 用户模块接口文档")
                        .version("1.0")
                        .description("用户登录、注册、个人信息相关接口")
                        .contact(new Contact().name("Wealth Team"))
                );
    }
}
