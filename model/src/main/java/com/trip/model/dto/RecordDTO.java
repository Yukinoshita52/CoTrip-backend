package com.trip.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ClassName: RecordDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 10:26
 * @Version 1.0
 */
@Data
public class RecordDTO {
    private Long bookId;
    private Integer type;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String note;
    private Date recordTime;
}
