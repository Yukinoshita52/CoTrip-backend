package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.model.vo.AnnouncementVO;
import com.trip.web.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表
     */
    @GetMapping
    public Result<List<AnnouncementVO>> getAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getAnnouncements();
        return Result.ok(announcements);
    }

    /**
     * 根据ID获取公告详情
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getAnnouncementById(id);
        return announcement != null ? Result.ok(announcement) : Result.error("公告不存在");
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public Result<List<AnnouncementVO>> getLatestAnnouncements(@RequestParam(defaultValue = "5") int count) {
        List<AnnouncementVO> announcements = announcementService.getLatestAnnouncements(count);
        return Result.ok(announcements);
    }

    /**
     * 创建公告
     */
    @PostMapping
    public Result<AnnouncementVO> createAnnouncement(@RequestBody AnnouncementVO announcementVO,
                                                     @org.springframework.security.core.annotation.AuthenticationPrincipal com.trip.common.login.LoginUser loginUser) {
        // 设置作者ID为当前登录用户
        if (loginUser != null) {
            announcementVO.setAuthorId(loginUser.getUserId());
        }
        AnnouncementVO created = announcementService.createAnnouncement(announcementVO);
        return Result.ok(created);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Result<AnnouncementVO> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementVO announcementVO) {
        announcementVO.setId(id);
        AnnouncementVO updated = announcementService.updateAnnouncement(announcementVO);
        return Result.ok(updated);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        return success ? Result.ok(true) : Result.error("删除失败");
    }

    /**
     * 搜索公告
     */
    @GetMapping("/search")
    public Result<List<AnnouncementVO>> searchAnnouncements(@RequestParam String keyword) {
        List<AnnouncementVO> announcements = announcementService.searchAnnouncements(keyword);
        return Result.ok(announcements);
    }
}