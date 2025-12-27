package com.trip.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 地点顺序更新 DTO
 */
@Data
public class PlaceOrderUpdateDTO {

    /**
     * 天数
     */
    @NotNull
    private Integer day;

    /**
     * 地点ID列表（按顺序排列）
     */
    @NotNull
    private List<Long> placeIds;
}
