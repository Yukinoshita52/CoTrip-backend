package com.trip.web.mapper;

import com.trip.model.dto.CommentCountDTO;
import com.trip.model.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.CommentVO;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
* @author 26423
* @description 针对表【comment】的数据库操作Mapper
* @createDate 2025-11-22 15:25:47
* @Entity com.trip.model.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {

    @MapKey("postId")
    Map<Long, CommentCountDTO> countByPostIds(List<Long> postIds);

    List<CommentVO> getCommentsByPostId(Long postId);

    /**
     * 如果本身评论是父评论，则返回所有子评论id
     * 如果是子评论，则删除其本身（因为查询不到以他为parent_id的其他记录）
     * @param commentId
     * @return
     */
    List<Long> getChildCommentIds(Long commentId);
}




