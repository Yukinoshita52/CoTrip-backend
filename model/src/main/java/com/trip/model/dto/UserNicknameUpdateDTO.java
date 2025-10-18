package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserNicknameUpdateDTO {

    @NotBlank
    private String nickname;
}


