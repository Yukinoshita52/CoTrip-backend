package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.model.dto.AuthLoginDTO;
import com.trip.model.dto.AuthRegisterDTO;
import com.trip.model.vo.AuthLoginVO;
import com.trip.model.vo.AuthRegisterVO;
import com.trip.web.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<AuthLoginVO> login(@RequestBody @Validated AuthLoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<AuthRegisterVO> register(@RequestBody @Validated AuthRegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }
}

