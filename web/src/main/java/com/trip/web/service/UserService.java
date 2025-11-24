package com.trip.web.service;

import com.trip.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.SearchUserVO;
import com.trip.model.vo.UserVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 26423
* @description 针对表【user(用户信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:17
*/
public interface UserService extends IService<User> {

    UserVO getCurrentUserInfo(Long userId);
    void updatePassword(Long userId, @NotBlank String oldPassword, @NotBlank String newPassword);
    void updateNickname(Long userId, String newNickname);
    String updateAvatar(Long userId, MultipartFile avatarFile);
    void updatePhone(Long userId, String phone);
    void deactivateAccount(Long userId, String password);
}
