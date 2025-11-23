package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.dto.CommentDTO;
import com.trip.model.entity.Comment;
import com.trip.model.vo.CommentCreatedVO;
import com.trip.model.vo.CommentListVO;
import com.trip.model.vo.CommentVO;
import com.trip.web.service.CommentService;
import com.trip.web.mapper.CommentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 26423
* @description 针对表【comment】的数据库操作Service实现
* @createDate 2025-11-22 15:25:47
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    @Resource
    private CommentMapper commentMapper;

    @Override
    public CommentListVO getCommentsByPostId(Long postId) {
        //查询帖子涉及到到评论（不分父子评论）
        List<CommentVO> commentVOS = commentMapper.getCommentsByPostId(postId);
        //对查询出来的commentVOS进行处理，区分父子评论
        Map<Long,List<CommentVO>> children = new HashMap<>();
        for(CommentVO commentVO : commentVOS){
            Long commentId = commentVO.getCommentId();
            Long parentId = commentVO.getParentId();

            //如果是父评论
            if(parentId == null && !children.containsKey(commentId)){
                children.put(commentId,new ArrayList<>());
            }
            //如果是子评论
            else if(parentId != null){
                if(!children.containsKey(parentId)){
                    children.put(parentId,new ArrayList<>());
                }
                children.get(parentId).add(commentVO);
            }
        }


        for(CommentVO commentVO : commentVOS){
            Long id = commentVO.getCommentId();
            //为每个父评论设置其子评论列表
            if(children.containsKey(id)){
                commentVO.setChildren(children.get(id));
            }else{
                //从commentVOS中删除子评论
                commentVOS.remove(commentVO);
            }
        }

        CommentListVO res = new CommentListVO();
        res.setPostId(postId);
        res.setComments(commentVOS);

        return res;
    }

    @Override
    public CommentCreatedVO addComment(CommentDTO dto, Long userId) {
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        commentMapper.insert(comment);

        CommentCreatedVO res = new CommentCreatedVO();
        res.setCommentId(comment.getId());
        res.setPostId(dto.getPostId());
        res.setCreateTime(comment.getCreateTime());
        return res;
    }
}




