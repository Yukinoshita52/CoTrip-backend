package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.BookUser;
import com.trip.web.service.BookUserService;
import com.trip.web.mapper.BookUserMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【book_user(-)】的数据库操作Service实现
* @createDate 2025-12-01 16:00:40
*/
@Service
public class BookUserServiceImpl extends ServiceImpl<BookUserMapper, BookUser>
    implements BookUserService{

}




