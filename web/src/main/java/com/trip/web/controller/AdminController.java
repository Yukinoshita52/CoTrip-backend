package com.trip.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.PromoteAdminDTO;
import com.trip.model.entity.User;
import com.trip.model.vo.AdminStatisticsVO;
import com.trip.model.vo.UserVO;
import com.trip.web.service.AdminStatisticsService;
import com.trip.web.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    
    /**
     * 搜索用户（通过用户名或手机号，仅管理员）
     */
    @GetMapping("/users/search")
    public Result<List<UserVO>> searchUsers(@RequestParam String keyword) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User currentUser = userService.getById(loginUser.getUserId());
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限访问");
        }
        
        // 通过用户名或手机号搜索
        List<User> users = userService.list(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getPhone, keyword)
                )
                .eq(User::getIsDeleted, 0)
                .last("LIMIT 20")); // 限制返回20条
        
        List<UserVO> userVOs = users.stream().map(user -> {
            UserVO vo = userService.getCurrentUserInfo(user.getId());
            return vo;
        }).collect(Collectors.toList());
        
        return Result.ok(userVOs);
    }
    
    /**
     * 提升用户为系统管理员（仅管理员可操作）
     */
    @PostMapping("/promote")
    public Result<Void> promoteToAdmin(@RequestBody @Valid PromoteAdminDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(loginUser.getUserId());
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限操作");
        }
        
        // 通过用户名或手机号查找目标用户
        User targetUser = userService.getOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper
                        .eq(User::getUsername, dto.getIdentifier())
                        .or()
                        .eq(User::getPhone, dto.getIdentifier())
                )
                .eq(User::getIsDeleted, 0));
        
        if (targetUser == null) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR.getCode(), "用户不存在");
        }
        
        // 如果已经是管理员，直接返回成功
        if (targetUser.getRole() != null && targetUser.getRole() == 1) {
            return Result.ok();
        }
        
        // 提升为管理员
        targetUser.setRole(1);
        userService.updateById(targetUser);
        
        return Result.ok();
    }
    
    /**
     * 取消用户的管理员权限（仅管理员可操作）
     */
    @PostMapping("/demote")
    public Result<Void> demoteFromAdmin(@RequestBody @Valid PromoteAdminDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查当前用户是否为管理员
        User currentUser = userService.getById(loginUser.getUserId());
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限操作");
        }
        
        // 不能取消自己的管理员权限
        if (currentUser.getUsername().equals(dto.getIdentifier()) || 
            (currentUser.getPhone() != null && currentUser.getPhone().equals(dto.getIdentifier()))) {
            return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "不能取消自己的管理员权限");
        }
        
        // 通过用户名或手机号查找目标用户
        User targetUser = userService.getOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper
                        .eq(User::getUsername, dto.getIdentifier())
                        .or()
                        .eq(User::getPhone, dto.getIdentifier())
                )
                .eq(User::getIsDeleted, 0));
        
        if (targetUser == null) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR.getCode(), "用户不存在");
        }
        
        // 如果不是管理员，直接返回成功
        if (targetUser.getRole() == null || targetUser.getRole() != 1) {
            return Result.ok();
        }
        
        // 取消管理员权限
        targetUser.setRole(0);
        userService.updateById(targetUser);
        
        return Result.ok();
    }
    
    /**
     * 获取所有管理员列表（仅管理员）
     */
    @GetMapping("/admins")
    public Result<List<UserVO>> getAllAdmins() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "请先登录");
        }
        
        // 检查管理员权限
        User currentUser = userService.getById(loginUser.getUserId());
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 1) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限访问");
        }
        
        // 查询所有管理员
        List<User> admins = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 1)
                .eq(User::getIsDeleted, 0));
        
        List<UserVO> adminVOs = admins.stream().map(user -> {
            UserVO vo = userService.getCurrentUserInfo(user.getId());
            return vo;
        }).collect(Collectors.toList());
        
        return Result.ok(adminVOs);
    }
}

