package com.wealth.common.exception;

import com.wealth.common.result.Result;
import com.wealth.common.utils.HttpResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(400, "参数校验失败：" + message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("参数约束违例: {}", e.getMessage());
        return Result.error(400, "参数校验失败：" + e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getMessage());
        return Result.error(400, "缺少请求参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getMessage());
        return Result.error(400, "参数类型错误：" + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(400, "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.error(405, "请求方法不支持");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getMessage());
        return Result.error(415, "不支持的媒体类型");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(404, "资源不存在");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性违例", e);
        return Result.error(500, "数据操作失败");
    }

    @ExceptionHandler(IllegalStateException.class)
    public void handleIllegalState(IllegalStateException e, HttpServletResponse response) {
        log.error("响应状态异常，直接写入错误响应", e);
        if (response.isCommitted()) return;
        try {
            HttpResponseUtil.writeJson(response, 500, 500, "系统错误");
        } catch (Exception ex) {
            log.warn("写入错误响应失败", ex);
        }
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("未捕获异常", e);
        return Result.error(500, "系统错误");
    }
}