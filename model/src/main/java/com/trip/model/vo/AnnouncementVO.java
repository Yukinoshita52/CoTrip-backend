package com.trip.model.vo;

import lombok.Data;

import java.util.Date;


@Data
public class AnnouncementVO {

    private Long id;

    private String title;

    private String content;

    private Date publishTime;

    private Long authorId;
}

