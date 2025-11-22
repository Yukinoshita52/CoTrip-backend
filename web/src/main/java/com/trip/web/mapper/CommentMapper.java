package com.trip.web.mapper;

import com.trip.model.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author 26423
* @description 针对表【comment】的数据库操作Mapper
* @createDate 2025-11-22 15:25:47
* @Entity com.trip.model.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {

    Map<Long, Integer> countByPostIds(List<Long> postIds);
}




