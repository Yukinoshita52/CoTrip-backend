package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginDTO {

    @NotBlank
    private String identifier; // username or phone

    @NotBlank
    private String password;
}


