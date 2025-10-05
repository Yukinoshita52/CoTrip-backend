package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 分享信息表
 * @TableName share
 */
@TableName(value ="share")
@Data
public class Share extends BaseEntity {
    /**
     * 行程ID
     */
    private Long tripId;
}