package com.trip.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.common.utils.JwtUtil;
import com.trip.common.utils.PasswordUtil;
import com.trip.model.dto.AuthLoginDTO;
import com.trip.model.dto.AuthRegisterDTO;
import com.trip.model.entity.User;
import com.trip.model.vo.AuthLoginVO;
import com.trip.model.vo.AuthRegisterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.trip.common.utils.PasswordUtil.isStrongPassword;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final GraphInfoService graphInfoService;

    public AuthLoginVO login(AuthLoginDTO dto) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getIdentifier()).or()
                .eq(User::getPhone, dto.getIdentifier()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }
        String token = JwtUtil.createToken(user.getId(), user.getUsername());
        AuthLoginVO vo = new AuthLoginVO();
        vo.setUserId(user.getId());
        vo.setToken(token);
        vo.setAvatarUrl(graphInfoService.getImageUrlById(user.getAvatarId()));
        return vo;
    }

    public AuthRegisterVO register(AuthRegisterDTO dto) {
        // 校验两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new LeaseException(ResultCodeEnum.PASSWORD_NOT_MATCH);
        }

        // 校验密码强度
        if (!isStrongPassword(dto.getPassword())) {
            throw new LeaseException(ResultCodeEnum.PASSWORD_WEAK);
        }

        // 检查用户名是否存在
        long exists = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (exists > 0) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST_ERROR);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAvatarId(0L); // 设置默认头像
        user.setNickname("未命名用户"); // 设置默认昵称
        userService.save(user);

        AuthRegisterVO vo = new AuthRegisterVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        return vo;
    }
}
