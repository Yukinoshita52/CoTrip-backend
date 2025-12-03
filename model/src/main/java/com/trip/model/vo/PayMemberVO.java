package com.trip.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: PayMemberVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 14:35
 * @Version 1.0
 */
@Data
public class PayMemberVO {
    private Long userId;
    private String nickname;
    private BigDecimal shouldPay;
}
