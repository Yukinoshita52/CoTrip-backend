package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.dto.CommentDTO;
import com.trip.model.entity.Comment;
import com.trip.model.entity.PostLike;
import com.trip.model.vo.*;
import com.trip.web.mapper.PostLikeMapper;
import com.trip.web.service.CommentService;
import com.trip.web.mapper.CommentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 26423
 * @description 针对表【comment】的数据库操作Service实现
 * @createDate 2025-11-22 15:25:47
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private PostLikeMapper postLikeMapper;

    @Override
    public CommentListVO getCommentsByPostId(Long postId) {
        //查询帖子涉及到到评论（不分父子评论）
        List<CommentVO> comments = commentMapper.getCommentsByPostId(postId);
        //对查询出来的commentVOS进行处理，区分父子评论
        Map<Long, List<CommentVO>> children = new HashMap<>();
        for (CommentVO comment : comments) {
            Long commentId = comment.getCommentId();
            Long parentId = comment.getParentId();

            //如果是父评论
            if (parentId == null && !children.containsKey(commentId)) {
                children.put(commentId, new ArrayList<>());
            }
            //如果是子评论
            else if (parentId != null) {
                if (!children.containsKey(parentId)) {
                    children.put(parentId, new ArrayList<>());
                }
                children.get(parentId).add(comment);
            }
        }


//        for (CommentVO comment : comments) {
//            Long id = comment.getCommentId();
//            //为每个父评论设置其子评论列表
//            if (children.containsKey(id)) {
//                comment.setChildren(children.get(id));
//            } else {
//                //从commentVOS中删除子评论
//                //这是一段错误代码！使用了增强for循环，在循环过程中又试图删除元素，会java.util.ConcurrentModificationException报错
//                comments.remove(comment);
//            }
//        }
        //解决方案，要么新的List来存，要么使用迭代器
        var iterator = comments.iterator();
        while(iterator.hasNext()){
            CommentVO comment = iterator.next();
            Long id = comment.getCommentId();

            // 为父评论设置其子评论列表
            if (children.containsKey(id)) {
                comment.setChildren(children.get(id));
            } else {
                // 如果不是父评论（即它是一个子评论，在 children Map 中没有键），则使用迭代器删除它
                iterator.remove(); // 安全的删除操作
            }
        }

        CommentListVO res = new CommentListVO();
        res.setPostId(postId);
        res.setComments(comments);

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

    @Override
    public CommentDeletedVO deleteComment(Long commentId) {
        List<Long> childCommentIds = commentMapper.getChildCommentIds(commentId);

        //修改逻辑为先删子评论、再删父评论，且删除子评论时要判断列表不为null、不为空
        if(childCommentIds != null && !childCommentIds.isEmpty()){
            commentMapper.deleteBatchIds(childCommentIds);
        }
        commentMapper.deleteById(commentId);


        CommentDeletedVO res = new CommentDeletedVO();
        res.setCommentId(commentId);
        res.setDeleted(true);
        return res;
    }

    @Override
    public PostLikeVO likePost(Long postId, Long userId) {
        // 检查用户是否已经点过赞
        PostLike existingLike = postLikeMapper.selectOne(
            new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)
        );
        
        if (existingLike != null) {
            // 用户已经点过赞，返回当前状态
            Integer likeCount = postLikeMapper.countByPostId(postId);
            return new PostLikeVO().withLiked(true).withPostId(postId).withLikeCount(likeCount);
        }
        
        // 用户未点赞，添加点赞记录
        try {
            PostLike postLike = new PostLike();
            postLike.setUserId(userId);
            postLike.setPostId(postId);
            postLikeMapper.insert(postLike);
            Integer likeCount = postLikeMapper.countByPostId(postId);
            return new PostLikeVO().withLiked(true).withPostId(postId).withLikeCount(likeCount);
        } catch (Exception e) {
            // 如果插入失败（可能是并发导致的重复插入），重新查询状态
            log.warn("点赞插入失败，可能是并发操作: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            Integer likeCount = postLikeMapper.countByPostId(postId);
            boolean isLiked = isPostLikedByUser(postId, userId);
            return new PostLikeVO().withLiked(isLiked).withPostId(postId).withLikeCount(likeCount);
        }
    }

    @Override
    public PostLikeVO unlikePost(Long postId, Long userId) {
        try {
            int deletedCount = postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                                        .eq(PostLike::getPostId, postId)
                                        .eq(PostLike::getUserId, userId));
            
            if (deletedCount > 0) {
                log.info("取消点赞成功: postId={}, userId={}", postId, userId);
            } else {
                log.warn("取消点赞时未找到记录: postId={}, userId={}", postId, userId);
            }
            
            Integer likeCount = postLikeMapper.countByPostId(postId);
            return new PostLikeVO().withLiked(false).withPostId(postId).withLikeCount(likeCount);
        } catch (Exception e) {
            log.error("取消点赞失败: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            // 即使删除失败，也返回当前状态
            Integer likeCount = postLikeMapper.countByPostId(postId);
            boolean isLiked = isPostLikedByUser(postId, userId);
            return new PostLikeVO().withLiked(isLiked).withPostId(postId).withLikeCount(likeCount);
        }
    }

    @Override
    public PostLikeUsersVO getPostLikeUsers(Long postId) {
        List<AuthorVO> users = postLikeMapper.getPostLikeUserByPostId(postId);

        PostLikeUsersVO res = new PostLikeUsersVO();
        res.setPostId(postId);
        res.setUsers(users);
        return res;
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        PostLike existingLike = postLikeMapper.selectOne(
            new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)
        );
        return existingLike != null;
    }

}




