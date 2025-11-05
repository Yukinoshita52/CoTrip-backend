package com.trip.model.entity;

import lombok.Data;
import java.util.Date;

/**
 * 公告实体类（对应announcement表）
 */
@Data
public class Announcement {
    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 作者ID
     */
    private Long authorId;
}
