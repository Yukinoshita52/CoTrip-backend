package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.UserNicknameUpdateDTO;
import com.trip.model.dto.UserPasswordDTO;
import com.trip.model.dto.UserPasswordUpdateDTO;
import com.trip.model.dto.UserPhoneUpdateDTO;
import com.trip.model.vo.ImageUrlVO;
import com.trip.model.vo.UserVO;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<UserVO> me() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        return Result.ok(userService.getCurrentUserInfo(loginUser.getUserId()));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Validated UserPasswordUpdateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        userService.updatePassword(loginUser.getUserId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.ok();
    }

    @PutMapping("/nickname")
    public Result<Void> updateNickname(@RequestBody @Validated UserNicknameUpdateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        userService.updateNickname(loginUser.getUserId(), dto.getNickname());
        return Result.ok();
    }

    @PutMapping("/avatar")
    public Result<ImageUrlVO> uploadAvatar(@RequestPart("file") MultipartFile file) {
        var loginUser = LoginUserHolder.getLoginUser();
        String url = userService.updateAvatar(loginUser.getUserId(), file);
        ImageUrlVO vo = new ImageUrlVO();
        vo.setUrl(url);
        return Result.ok(vo);
    }

    @PutMapping("/phone")
    public Result<Void> updatePhone(@RequestBody @Validated UserPhoneUpdateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        userService.updatePhone(loginUser.getUserId(), dto.getPhone());
        return Result.ok();
    }

    // 注销账号
    @DeleteMapping
    public Result<Void> deleteAccount(@RequestBody @Validated UserPasswordDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        userService.deactivateAccount(loginUser.getUserId(), dto.getPassword());
        return Result.ok();
    }
}


