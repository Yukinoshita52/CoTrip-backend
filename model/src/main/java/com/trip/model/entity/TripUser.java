package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行程-用户关联表
 * @TableName trip_user
 */
@TableName(value = "trip_user")
@Data
@EqualsAndHashCode(callSuper = false)
public class TripUser extends BaseEntity {
    /**
     * 行程ID
     */
    private Long tripId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色：0-创建者，1-管理员，2-参与者
     */
    private Integer role;
}

