package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 创建行程 DTO
 */
@Data
public class TripCreateDTO {

    @NotBlank(message = "行程名称不能为空")
    private String name;

    @NotNull(message = "开始时间不能为空")
    private Date startDate;

    @NotNull(message = "结束时间不能为空")
    private Date endDate;

    private String description;

    private String region;
    
    private String coverImageUrl;
}

