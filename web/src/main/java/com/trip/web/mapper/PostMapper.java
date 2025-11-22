package com.trip.web.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.model.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.FeedPageVO;
import com.trip.model.vo.StatVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
* @author 26423
* @description 针对表【post】的数据库操作Mapper
* @createDate 2025-11-22 15:25:47
* @Entity com.trip.model.entity.Post
*/
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 通过postId获取这篇帖子的状态（点赞数、评论数、用户是否点赞【Nullable】）
     * 用户是否点赞字段可能不会返回，取决于是否传入userId参数
     * @param postId
     * @param userId
     * @return
     */
    StatVO getStatsByPostId(@Param("postId") Long postId, @Nullable @Param("userId") Long userId);
}




