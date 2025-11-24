package com.trip.common.exception;

import com.trip.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常（可以返回给前端）
     */
    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result<?> handleLeaseException(LeaseException e){
        log.error("[BizException] code={}, msg={}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常（不返回真实错误）
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(Exception e){
        log.error("[SystemException] {}", e.getMessage(), e);
        return Result.fail(500, "系统异常，请稍后再试");
    }
}