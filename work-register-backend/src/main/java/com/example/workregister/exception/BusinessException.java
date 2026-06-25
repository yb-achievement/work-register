package com.example.workregister.exception;

/**
 * 业务异常，用于承载可直接返回给前端的提示信息。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
