package com.trip.common.result;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局统一返回结果类
 */
@Data
public class Result<T> {

    //返回码
    private Integer code;

    //返回消息
    private String message;

    //返回数据
    private T data;

    public Result() {
    }

    private static <T> Result<T> build(T data) {
        Result<T> result = new Result<>();
        if (data != null)
            result.setData(data);
        return result;
    }

    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }


    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    public static <T> Result<T> fail() {
        return build(null, ResultCodeEnum.FAIL);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> fail(Integer code,String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        // 设置data为空对象{}，确保符合接口定义（object类型）
        Map<String, Object> emptyData = new LinkedHashMap<>();
        result.setData((T) emptyData);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return fail(ResultCodeEnum.FAIL.getCode(), message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }
}
