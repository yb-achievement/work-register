package com.example.workregister.exception;

import com.example.workregister.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，将校验异常、业务异常和未知异常统一包装为 Result。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常直接返回业务提示。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.fail(400, exception.getMessage());
    }

    /**
     * 请求体字段校验失败时，汇总每个字段的错误提示。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.fail(400, message);
    }

    /**
     * URL 参数或路径变量校验失败时返回原始校验信息。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return Result.fail(400, exception.getMessage());
    }

    /**
     * 兜底处理未预期异常，避免直接暴露框架默认错误页。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.fail(500, "系统异常：" + exception.getMessage());
    }
}
