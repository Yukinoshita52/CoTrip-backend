package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.User;
import com.trip.model.vo.AnnouncementVO;
import com.trip.web.service.AnnouncementService;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;

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
     * 创建公告（仅管理员）
     */
    @PostMapping
    public Result<AnnouncementVO> createAnnouncement(@RequestBody AnnouncementVO announcementVO) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User user = userService.getById(loginUser.getUserId());
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限创建公告");
        }
        
        announcementVO.setAuthorId(loginUser.getUserId());
        AnnouncementVO created = announcementService.createAnnouncement(announcementVO);
        return Result.ok(created);
    }

    /**
     * 更新公告（仅管理员）
     */
    @PutMapping("/{id}")
    public Result<AnnouncementVO> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementVO announcementVO) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User user = userService.getById(loginUser.getUserId());
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限修改公告");
        }
        
        announcementVO.setId(id);
        AnnouncementVO updated = announcementService.updateAnnouncement(announcementVO);
        return Result.ok(updated);
    }

    /**
     * 删除公告（仅管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnnouncement(@PathVariable Long id) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User user = userService.getById(loginUser.getUserId());
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限删除公告");
        }
        
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