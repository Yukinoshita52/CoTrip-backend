package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.User;
import com.trip.web.service.UserService;
import com.trip.web.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【user(用户信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:17
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




