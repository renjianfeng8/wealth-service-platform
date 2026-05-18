package com.wealth.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计日志注解。
 * 标注在 Controller 方法上，由 AuditLogAspect 自动记录操作详情（用户、IP、参数、耗时等）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 所属模块，如 "系统管理"、"用户管理"、"交易委托" */
    String module();

    /** 操作描述，如 "新增管理员"、"创建委托单" */
    String operation();
}
