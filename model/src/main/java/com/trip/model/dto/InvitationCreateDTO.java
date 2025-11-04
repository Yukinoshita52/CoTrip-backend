package com.trip.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class InvitationCreateDTO {

    @NotBlank(message = "被邀请人手机号不能为空")
    private String invitee;
}

