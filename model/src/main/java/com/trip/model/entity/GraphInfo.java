package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 图片信息表
 * @TableName graph_info
 */
@TableName(value ="graph_info")
@Data
public class GraphInfo extends BaseEntity {
    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片所属对象类型：1-用户头像，2-行程图片，3-地点图片，4-其他
     */
    private Integer itemType;

    /**
     * 图片所属对象ID
     */
    private Long itemId;

    /**
     * 图片地址
     */
    private String url;
}