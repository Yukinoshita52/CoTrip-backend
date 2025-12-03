package com.trip.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.model.entity.User;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ClassName: RecordVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 10:33
 * @Version 1.0
 */
@Data
public class RecordVO {

    private Long recordId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal amount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long iconId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String note;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date recordTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AuthorVO user;
}
