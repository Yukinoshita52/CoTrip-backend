package com.trip.web.mapper;

import com.trip.model.dto.LikeCountDTO;
import com.trip.model.entity.PostLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.AuthorVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 26423
* @description 针对表【post_like】的数据库操作Mapper
* @createDate 2025-11-22 15:25:47
* @Entity com.trip.model.entity.PostLike
*/
public interface PostLikeMapper extends BaseMapper<PostLike> {

    @MapKey("postId")
    Map<Long, LikeCountDTO> countByPostIds(List<Long> postIds);

    Integer countByPostId(Long postId);

    List<AuthorVO> getPostLikeUserByPostId(Long postId);
    
    /**
     * 获取不同的帖子ID列表（用于数据同步）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 帖子ID列表
     */
    List<Long> getDistinctPostIds(@Param("offset") int offset, @Param("limit") int limit);
}




