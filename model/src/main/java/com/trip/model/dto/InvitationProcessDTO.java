package com.trip.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理邀请 DTO
 */
@Data
public class InvitationProcessDTO {


    @NotNull(message = "邀请ID不能为空")
    private Long invitationId;


    @NotNull(message = "处理结果不能为空")
    private Integer action; // 1-同意，2-拒绝
}

