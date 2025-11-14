package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邀请信息表
 * @TableName invitation
 */
@TableName(value ="invitation")
@Data
@EqualsAndHashCode(callSuper = false)
public class Invitation extends BaseEntity {
    /**
     * 行程ID
     */
    private Long tripId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人手机号
     */
    private String invitee;

    /**
     * 邀请状态：0-待接受，1-已接受，2-已拒绝，3-已过期
     */
    private Integer status;
}