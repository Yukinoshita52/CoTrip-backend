package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordUpdateDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}


