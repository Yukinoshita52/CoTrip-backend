package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Post;
import com.trip.web.service.PostService;
import com.trip.web.mapper.PostMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【post】的数据库操作Service实现
* @createDate 2025-11-22 15:25:47
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

}




