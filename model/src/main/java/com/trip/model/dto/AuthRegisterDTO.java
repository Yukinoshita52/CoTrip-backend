package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRegisterDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}


