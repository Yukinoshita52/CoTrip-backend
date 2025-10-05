package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 邀请信息表
 * @TableName invitation
 */
@TableName(value ="invitation")
@Data
public class Invitation extends BaseEntity {
    /**
     * 被邀请人手机号
     */
    private String invitee;

    /**
     * 邀请状态：0-待接受，1-已接受，2-已拒绝，3-已过期
     */
    private Integer status;
}