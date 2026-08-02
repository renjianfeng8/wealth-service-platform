package com.wealth.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置契约测试：H1/H2 修复的回归保护。
 * 验证含连字符的 kebab-case 属性（jwt.access-expire）能被环境变量
 * JWT_ACCESS_EXPIRE 通过 Spring Boot 宽松绑定解析，且真实 yml 的
 * 占位符默认值满足 JwtUtil ≥32 字节密钥硬校验。
 */
class JwtConfigContractTest {

    private StandardEnvironment buildEnvironment(Map<String, Object> sysEnv) {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().replace("systemEnvironment",
                new SystemEnvironmentPropertySource("systemEnvironment", sysEnv));
        // 复刻 SpringApplication 的环境宽松绑定包装器
        ConfigurationPropertySources.attach(env);
        return env;
    }

    private PropertySource<?> loadYaml(String name) throws Exception {
        return new YamlPropertySourceLoader()
                .load(name, new ClassPathResource(name)).get(0);
    }

    @Test
    void underscore_env_should_relax_bind_to_dashed_jwt_property() {
        Map<String, Object> sysEnv = new HashMap<>();
        sysEnv.put("JWT_ACCESS_EXPIRE", "1111");
        sysEnv.put("JWT_REFRESH_EXPIRE", "2222");

        StandardEnvironment env = buildEnvironment(sysEnv);

        assertThat(env.getProperty("jwt.access-expire")).isEqualTo("1111");
        assertThat(env.getProperty("jwt.refresh-expire")).isEqualTo("2222");
    }

    @Test
    void dev_yml_should_load_and_env_should_override_jwt_expire() throws Exception {
        PropertySource<?> yml = loadYaml("application.yml");

        Map<String, Object> sysEnv = new HashMap<>();
        sysEnv.put("JWT_ACCESS_EXPIRE", "1111");
        sysEnv.put("JWT_REFRESH_EXPIRE", "2222");
        StandardEnvironment env = buildEnvironment(sysEnv);
        env.getPropertySources().addLast(yml); // 优先级: 环境变量 > yml

        assertThat(env.resolvePlaceholders("${jwt.access-expire}")).isEqualTo("1111");
        assertThat(env.resolvePlaceholders("${jwt.refresh-expire}")).isEqualTo("2222");

        String secret = env.resolvePlaceholders("${jwt.secret}");
        assertThat(secret.getBytes(StandardCharsets.UTF_8).length).isGreaterThanOrEqualTo(32);
    }

    @Test
    void dev_yml_should_fallback_to_jwt_defaults_when_env_missing() throws Exception {
        PropertySource<?> yml = loadYaml("application.yml");
        StandardEnvironment env = buildEnvironment(Collections.emptyMap());
        env.getPropertySources().addLast(yml);

        assertThat(env.resolvePlaceholders("${jwt.access-expire}")).isEqualTo("1800000");
        assertThat(env.resolvePlaceholders("${jwt.refresh-expire}")).isEqualTo("604800000");
    }

    @Test
    void prod_yml_should_define_live_expire_keys_and_no_dead_expire() throws Exception {
        PropertySource<?> prod = loadYaml("application-prod.yml");

        assertThat(prod.getProperty("jwt.access-expire")).isEqualTo("${JWT_ACCESS_EXPIRE:1800000}");
        assertThat(prod.getProperty("jwt.refresh-expire")).isEqualTo("${JWT_REFRESH_EXPIRE:604800000}");
        assertThat(prod.getProperty("jwt.expire")).isNull();
    }

    @Test
    void jwt_util_should_inject_env_derived_values_in_real_boot_context() {
        new ApplicationContextRunner()
                .withInitializer(context -> {
                    StandardEnvironment env = new StandardEnvironment();
                    Map<String, Object> sysEnv = new HashMap<>();
                    sysEnv.put("JWT_SECRET", "abcdefghijklmnopqrstuvwxyz1234567890");
                    sysEnv.put("JWT_ACCESS_EXPIRE", "12345");
                    sysEnv.put("JWT_REFRESH_EXPIRE", "999999");
                    env.getPropertySources().replace("systemEnvironment",
                            new SystemEnvironmentPropertySource("systemEnvironment", sysEnv));
                    ConfigurationPropertySources.attach(env);
                    context.setEnvironment(env);
                })
                .withBean(PropertySourcesPlaceholderConfigurer.class)
                .withBean(JwtUtil.class)
                .run(context -> {
                    JwtUtil jwtUtil = context.getBean(JwtUtil.class);

                    // @Value 注入的环境变量值
                    assertThat((Long) ReflectionTestUtils.getField(jwtUtil, "accessExpire")).isEqualTo(12345L);
                    assertThat((Long) ReflectionTestUtils.getField(jwtUtil, "refreshExpire")).isEqualTo(999999L);

                    // 密钥 ≥32 字节且 init() 成功构建签名密钥
                    String secret = (String) ReflectionTestUtils.getField(jwtUtil, "secretKey");
                    assertThat(secret.getBytes(StandardCharsets.UTF_8).length).isGreaterThanOrEqualTo(32);
                    assertThat(ReflectionTestUtils.getField(jwtUtil, "cachedSigningKey")).isNotNull();

                    // 端到端: 注入后的密钥/时效可正常生成并校验 token
                    JwtUtil.TokenPair pair = jwtUtil.generateTokenPair("admin");
                    assertThat(jwtUtil.validateToken(pair.accessToken())).isTrue();
                });
    }
}
