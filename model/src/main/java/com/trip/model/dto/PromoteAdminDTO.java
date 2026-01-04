package com.trip.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 提升/取消管理员 DTO
 */
@Data
public class PromoteAdminDTO {
    /**
     * 用户名或手机号
     */
    @NotBlank(message = "用户名或手机号不能为空")
    private String identifier;
}


