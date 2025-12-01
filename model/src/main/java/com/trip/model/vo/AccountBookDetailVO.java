package com.trip.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: AccountBookDetailVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/29 16:10
 * @Version 1.0
 */
@Data
public class AccountBookDetailVO {
    private Long bookId;
    private String name;
    private Long tripId;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}
