package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.User;
import com.trip.model.vo.AdminStatisticsVO;
import com.trip.web.service.AdminStatisticsService;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminStatisticsService adminStatisticsService;
    private final UserService userService;
    
    /**
     * 检查当前用户是否为管理员
     */
    @GetMapping("/check")
    public Result<Boolean> checkAdmin() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        User user = userService.getById(loginUser.getUserId());
        boolean isAdmin = user != null && user.getRole() != null && user.getRole() == 1;
        return Result.ok(isAdmin);
    }
    
    /**
     * 获取系统统计数据（仅管理员）
     */
    @GetMapping("/statistics")
    public Result<AdminStatisticsVO> getStatistics() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User user = userService.getById(loginUser.getUserId());
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限访问");
        }
        
        AdminStatisticsVO statistics = adminStatisticsService.getStatistics();
        return Result.ok(statistics);
    }
}

