package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地点信息表
 * @TableName place
 */
@TableName(value ="place")
@Data
public class Place {
    private Long id;
    private String name;
    private Integer typeId;
    private String uid;
    private Float lat;
    private Float lng;
    private String address;
    private String telephone;
    private String detailInfo;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer isDeleted;
}
