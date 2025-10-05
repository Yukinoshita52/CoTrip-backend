package com.trip.common.exception;

import com.trip.common.result.ResultCodeEnum;
import lombok.Data;

/**
 * ClassName: LeaseException
 * Package: com.yukino.lease.common.exception
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/18 20:16
 * @Version 1.0
 */
@Data
public class LeaseException extends RuntimeException{
    private Integer code;

    public LeaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public LeaseException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
