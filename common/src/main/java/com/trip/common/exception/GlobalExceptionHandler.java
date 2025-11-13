package com.trip.common.exception;

import com.trip.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ClassName: GlobalExceptionHandler
 * Package: com.yukino.lease.common.exception
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/17 18:26
 * @Version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> error(Exception e){
        e.printStackTrace();
        System.err.println("捕获到异常: " + e.getClass().getName());
        System.err.println("异常消息: " + e.getMessage());
        // 返回错误信息而不是null
        return Result.fail(201, e.getMessage() != null ? e.getMessage() : "操作失败");
    }

    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result handle(LeaseException e){
        e.printStackTrace();
        return Result.fail(e.getCode(), e.getMessage());
    }
}