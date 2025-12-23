package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.User;
import com.trip.model.vo.UserVO;
import com.trip.web.service.UserService;
import com.trip.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.trip.common.utils.PasswordUtil.isStrongPassword;

/**
* @author 26423
* @description 针对表【user(用户信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:17
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final GraphInfoServiceImpl graphInfoService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO getCurrentUserInfo(Long userId){
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(graphInfoService.getImageUrlById(user.getAvatarId()));
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LeaseException(ResultCodeEnum.APP_ORIGINAL_PASSWORD_ERROR);
        }
        if (!isStrongPassword(newPassword)) {
            throw new LeaseException(ResultCodeEnum.PASSWORD_WEAK);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public void updateNickname(Long userId, String newNickname) {
        User user = this.getById(userId);
        if (user == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        user.setNickname(newNickname);
        this.updateById(user);
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile avatarFile) {
        User user = this.getById(userId);
        if (user == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 删除旧头像
        Long oldGraphId = user.getAvatarId();
        if (oldGraphId != null && oldGraphId != 1L) {
            try {
                graphInfoService.deleteImageById(oldGraphId);
            } catch (Exception e) {
                log.warn("删除旧头像失败");
            }
        }

        // 上传图片并生成 graph 记录
        Long graphId = graphInfoService.uploadImage(avatarFile, 1, userId);

        // 更新用户的 avatarId
        user.setAvatarId(graphId);
        this.updateById(user);

        // 返回可访问链接
        return graphInfoService.getImageUrlById(graphId);
    }

    @Override
    public void updatePhone(Long userId, String phone) {
        User user = this.getById(userId);
        if (user == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        if (this.lambdaQuery().eq(User::getPhone, phone).ne(User::getId, userId).count() > 0) {
            throw new LeaseException(ResultCodeEnum.APP_PHONE_EXIST_ERROR);
        }

        user.setPhone(phone);
        this.updateById(user);
    }

    @Override
    public void deactivateAccount(Long userId, String password) {
        User user = this.getById(userId);
        if (user == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        // 校验加密密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LeaseException(ResultCodeEnum.APP_ORIGINAL_PASSWORD_ERROR);
        }

        // 逻辑删除用户
        user.setIsDeleted((byte) 1);
        this.updateById(user);
    }
}
