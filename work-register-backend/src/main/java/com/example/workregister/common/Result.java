package com.example.workregister.common;

/**
 * 统一接口返回结构。
 *
 * @param <T> data 字段的数据类型
 */
public class Result<T> {

    /**
     * 业务状态码。约定 0 表示成功，非 0 表示失败。
     */
    private Integer code;

    /**
     * 面向调用方展示的结果说明。
     */
    private String message;

    /**
     * 成功时返回的业务数据。
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造无业务数据的成功响应。
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    /**
     * 构造带业务数据的成功响应。
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    /**
     * 构造默认失败响应。
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(-1, message, null);
    }

    /**
     * 构造指定业务状态码的失败响应。
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
