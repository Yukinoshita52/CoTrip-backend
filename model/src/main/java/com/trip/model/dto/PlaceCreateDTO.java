package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceCreateDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String typeCode; // maps to place_type.code

    @NotNull
    private Integer day;

    private String notes;
}


