package com.trip.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: MemberPayDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 14:45
 * @Version 1.0
 */
@Data
public class MemberPayDTO {
    private Long userId;
    private String nickname;
    private BigDecimal expense;
}
