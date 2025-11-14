package com.trip.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 邀请信息 VO
 */
@Data
public class InvitationVO {
    /**
     * 邀请ID
     */
    private Long invitationId;

    /**
     * 行程ID
     */
    private Long tripId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 邀请人昵称
     */
    private String inviterNickname;

    /**
     * 邀请人头像URL
     */
    private String inviterAvatarUrl;

    /**
     * 邀请人手机号
     */
    private String inviterPhone;

    /**
     * 被邀请人手机号
     */
    private String invitee;

    /**
     * 邀请状态：0-待接受，1-已接受，2-已拒绝，3-已过期
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date sentTime;
}

