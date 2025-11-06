package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.model.vo.AnnouncementVO;
import com.trip.web.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表
     * @return 公告列表
     */
    @GetMapping
    public Result<List<AnnouncementVO>> getAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getAnnouncements();
        return Result.ok(announcements);
    }
}

