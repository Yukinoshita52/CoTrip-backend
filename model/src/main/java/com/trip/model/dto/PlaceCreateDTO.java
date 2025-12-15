package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceCreateDTO {

    @NotBlank
    private String uid;

    @NotNull
    private Integer day;

    /**
     * 地点类型ID（可选）
     */
    private Integer typeId;
}


