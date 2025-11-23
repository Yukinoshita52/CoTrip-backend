package com.trip.web.service;

import com.trip.model.dto.CommentDTO;
import com.trip.model.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.CommentCreatedVO;
import com.trip.model.vo.CommentListVO;

/**
* @author 26423
* @description 针对表【comment】的数据库操作Service
* @createDate 2025-11-22 15:25:47
*/
public interface CommentService extends IService<Comment> {

    CommentListVO getCommentsByPostId(Long postId);

    CommentCreatedVO addComment(CommentDTO dto, Long userId);
}
