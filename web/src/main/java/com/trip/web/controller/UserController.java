package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.*;
import com.trip.model.vo.ImageUrlVO;
import com.trip.model.vo.UserVO;
import com.trip.web.service.UserService;
import com.trip.common.login.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 获取当前登录用户信息
    @GetMapping
    public Result<UserVO> me(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(),
                    ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        return Result.ok(userService.getCurrentUserInfo(loginUser.getUserId()));
    }

    // 修改密码
    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody @Validated UserPasswordUpdateDTO dto) {
        userService.updatePassword(loginUser.getUserId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.ok();
    }

    // 修改昵称
    @PutMapping("/nickname")
    public Result<Void> updateNickname(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody @Validated UserNicknameUpdateDTO dto) {
        userService.updateNickname(loginUser.getUserId(), dto.getNickname());
        return Result.ok();
    }

    // 上传头像
    @PutMapping("/avatar")
    public Result<ImageUrlVO> uploadAvatar(@AuthenticationPrincipal LoginUser loginUser,
                                           @RequestPart("file") MultipartFile file) {
        String url = userService.updateAvatar(loginUser.getUserId(), file);
        ImageUrlVO vo = new ImageUrlVO();
        vo.setUrl(url);
        return Result.ok(vo);
    }

    // 修改手机号
    @PutMapping("/phone")
    public Result<Void> updatePhone(@AuthenticationPrincipal LoginUser loginUser,
                                    @RequestBody @Validated UserPhoneUpdateDTO dto) {
        userService.updatePhone(loginUser.getUserId(), dto.getPhone());
        return Result.ok();
    }

    // 注销账号
    @DeleteMapping
    public Result<Void> deleteAccount(@AuthenticationPrincipal LoginUser loginUser,
                                      @RequestBody @Validated UserPasswordDTO dto) {
        userService.deactivateAccount(loginUser.getUserId(), dto.getPassword());
        return Result.ok();
    }
}
