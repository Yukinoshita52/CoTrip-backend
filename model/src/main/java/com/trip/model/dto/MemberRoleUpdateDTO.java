package com.trip.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 修改成员角色 DTO
 */
@Data
public class MemberRoleUpdateDTO {
    /**
     * 目标用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 新角色：0-创建者，1-管理员，2-参与者
     */
    @NotNull(message = "角色不能为空")
    private Integer role;
}

