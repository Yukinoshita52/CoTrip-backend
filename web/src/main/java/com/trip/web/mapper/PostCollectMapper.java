package com.trip.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.entity.PostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子收藏表 Mapper 接口
 */
@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {
    
    /**
     * 获取帖子收藏数
     */
    @Select("SELECT COUNT(*) FROM post_collect WHERE post_id = #{postId} AND is_deleted = 0")
    Long getCollectCount(@Param("postId") Long postId);
    
    /**
     * 检查用户是否已收藏帖子
     */
    @Select("SELECT COUNT(*) > 0 FROM post_collect WHERE post_id = #{postId} AND user_id = #{userId} AND is_deleted = 0")
    Boolean isCollectedByUser(@Param("postId") Long postId, @Param("userId") Long userId);
}