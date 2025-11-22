package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Comment;
import com.trip.web.service.CommentService;
import com.trip.web.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【comment】的数据库操作Service实现
* @createDate 2025-11-22 15:25:47
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




