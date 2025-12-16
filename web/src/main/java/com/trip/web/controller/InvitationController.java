package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.vo.InvitationVO;
import com.trip.web.service.InvitationService;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邀请控制器
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    /**
     * 查看我发出的邀请
     * @return 邀请列表
     */
    @GetMapping("/sent")
    public Result<List<InvitationVO>> getSentInvitations() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        List<InvitationVO> invitations = invitationService.getSentInvitations(loginUser.getUserId());
        return Result.ok(invitations);
    }

    /**
     * 查看我收到的邀请（待处理邀请）
     * @return 邀请列表
     */
    @GetMapping("/received")
    public Result<List<InvitationVO>> getReceivedInvitations() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        // 获取当前用户手机号
        var user = userService.getById(loginUser.getUserId());
        if (user == null || user.getPhone() == null) {
            return Result.fail(ResultCodeEnum.DATA_ERROR.getCode(), "用户手机号不存在");
        }
        
        List<InvitationVO> invitations = invitationService.getReceivedInvitations(user.getPhone());
        return Result.ok(invitations);
    }

    /**
     * 接受邀请
     * @param invitationId 邀请ID
     * @return 结果
     */
    @PostMapping("/{invitationId}/accept")
    public Result<Void> acceptInvitation(@PathVariable Long invitationId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        invitationService.processInvitation(invitationId, 1); // 1-接受
        return Result.ok();
    }

    /**
     * 拒绝邀请
     * @param invitationId 邀请ID
     * @return 结果
     */
    @PostMapping("/{invitationId}/reject")
    public Result<Void> rejectInvitation(@PathVariable Long invitationId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        invitationService.processInvitation(invitationId, 2); // 2-拒绝
        return Result.ok();
    }

    /**
     * 撤销邀请（发出的邀请）
     * @param invitationId 邀请ID
     * @return 结果
     */
    @DeleteMapping("/{invitationId}")
    public Result<Void> cancelInvitation(@PathVariable Long invitationId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        invitationService.cancelInvitation(invitationId, loginUser.getUserId());
        return Result.ok();
    }

    /**
     * 删除收到的邀请
     * @param invitationId 邀请ID
     * @return 结果
     */
    @DeleteMapping("/received/{invitationId}")
    public Result<Void> deleteReceivedInvitation(@PathVariable Long invitationId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        // 获取当前用户手机号
        var user = userService.getById(loginUser.getUserId());
        if (user == null || user.getPhone() == null) {
            return Result.fail(ResultCodeEnum.DATA_ERROR.getCode(), "用户手机号不存在");
        }
        
        invitationService.deleteReceivedInvitation(invitationId, user.getPhone());
        return Result.ok();
    }
}

