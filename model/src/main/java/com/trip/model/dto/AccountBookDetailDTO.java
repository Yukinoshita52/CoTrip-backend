package com.trip.model.dto;

/**
 * ClassName: AccountBookDetailDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/29 16:44
 * @Version 1.0
 */

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountBookDetailDTO {
    private Long bookId;
    private String name;
    private Long tripId;
    private BigDecimal totalAmount;
    private Integer type;
}
