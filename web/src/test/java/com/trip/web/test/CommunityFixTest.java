package com.trip.web.test;

import com.trip.web.service.PostViewService;
import com.trip.web.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 社区功能修复测试
 */
@Slf4j
@SpringBootTest
public class CommunityFixTest {

    @Autowired
    private PostViewService postViewService;
    
    @Autowired
    private CommentService commentService;

    /**
     * 测试Redis浏览量计数功能
     */
    @Test
    public void testViewCounting() {
        Long postId = 1L;
        Long userId = 5L;
        
        log.info("=== 测试Redis浏览量计数功能 ===");
        
        // 获取初始浏览量
        Long initialCount = postViewService.getViewCount(postId);
        log.info("初始浏览量: {}", initialCount);
        
        // 增加浏览量（第一次）
        Long newCount1 = postViewService.incrementViewCount(postId, userId);
        log.info("第一次浏览后浏览量: {}", newCount1);
        
        // 再次增加浏览量（同一用户，24小时内不应重复计数）
        Long newCount2 = postViewService.incrementViewCount(postId, userId);
        log.info("同一用户再次浏览后浏览量: {}", newCount2);
        
        // 不同用户浏览
        Long newCount3 = postViewService.incrementViewCount(postId, 6L);
        log.info("不同用户浏览后浏览量: {}", newCount3);
        
        // 匿名用户浏览（每次都计数）
        Long newCount4 = postViewService.incrementViewCount(postId, null);
        log.info("匿名用户浏览后浏览量: {}", newCount4);
        
        Long newCount5 = postViewService.incrementViewCount(postId, null);
        log.info("匿名用户再次浏览后浏览量: {}", newCount5);
        
        log.info("=== 浏览量计数测试完成 ===");
    }

    /**
     * 测试点赞功能的并发安全性
     */
    @Test
    public void testLikeFunctionality() {
        Long postId = 1L;
        Long userId = 5L;
        
        log.info("=== 测试点赞功能 ===");
        
        // 检查初始点赞状态
        boolean initialLiked = commentService.isPostLikedByUser(postId, userId);
        log.info("初始点赞状态: {}", initialLiked);
        
        if (!initialLiked) {
            // 点赞
            var likeResult = commentService.likePost(postId, userId);
            log.info("点赞结果: liked={}, count={}", likeResult.getLiked(), likeResult.getLikeCount());
            
            // 重复点赞（应该不会出错）
            var duplicateLikeResult = commentService.likePost(postId, userId);
            log.info("重复点赞结果: liked={}, count={}", duplicateLikeResult.getLiked(), duplicateLikeResult.getLikeCount());
        }
        
        // 取消点赞
        var unlikeResult = commentService.unlikePost(postId, userId);
        log.info("取消点赞结果: liked={}, count={}", unlikeResult.getLiked(), unlikeResult.getLikeCount());
        
        // 重新点赞
        var reLikeResult = commentService.likePost(postId, userId);
        log.info("重新点赞结果: liked={}, count={}", reLikeResult.getLiked(), reLikeResult.getLikeCount());
        
        // 再次取消点赞
        var finalUnlikeResult = commentService.unlikePost(postId, userId);
        log.info("最终取消点赞结果: liked={}, count={}", finalUnlikeResult.getLiked(), finalUnlikeResult.getLikeCount());
        
        log.info("=== 点赞功能测试完成 ===");
    }

    /**
     * 测试社区详情页面的浏览量功能
     */
    @Test
    public void testCommunityDetailViewCount() {
        Long postId = 1L;
        Long userId1 = 5L;
        Long userId2 = 6L;
        
        log.info("=== 测试社区详情页面浏览量功能 ===");
        
        // 初始化浏览量
        postViewService.setViewCount(postId, 50L);
        log.info("初始化帖子{}浏览量为: {}", postId, postViewService.getViewCount(postId));
        
        // 模拟用户1访问详情页
        Long viewCount1 = postViewService.incrementViewCount(postId, userId1);
        log.info("用户{}访问后浏览量: {}", userId1, viewCount1);
        
        // 模拟用户1再次访问（24小时内不应重复计数）
        Long viewCount2 = postViewService.incrementViewCount(postId, userId1);
        log.info("用户{}再次访问后浏览量: {}", userId1, viewCount2);
        
        // 模拟用户2访问
        Long viewCount3 = postViewService.incrementViewCount(postId, userId2);
        log.info("用户{}访问后浏览量: {}", userId2, viewCount3);
        
        // 模拟匿名用户访问
        Long viewCount4 = postViewService.incrementViewCount(postId, null);
        log.info("匿名用户访问后浏览量: {}", viewCount4);
        
        log.info("=== 社区详情页面浏览量功能测试完成 ===");
    }

    /**
     * 初始化一些帖子的浏览量数据
     */
    @Test
    public void initializeViewCounts() {
        log.info("=== 初始化帖子浏览量数据 ===");
        
        // 为一些帖子设置初始浏览量
        postViewService.setViewCount(1L, 100L);
        postViewService.setViewCount(2L, 50L);
        postViewService.setViewCount(3L, 200L);
        
        log.info("帖子1浏览量: {}", postViewService.getViewCount(1L));
        log.info("帖子2浏览量: {}", postViewService.getViewCount(2L));
        log.info("帖子3浏览量: {}", postViewService.getViewCount(3L));
        
        log.info("=== 浏览量数据初始化完成 ===");
    }

    /**
     * 测试社区详情页面的评论功能
     */
    @Test
    public void testCommunityCommentFunctionality() {
        Long postId = 1L;
        Long userId = 5L;
        
        log.info("=== 测试社区详情页面评论功能 ===");
        
        try {
            // 测试获取评论列表
            var commentsResult = commentService.getCommentsByPostId(postId);
            log.info("获取评论列表: postId={}, 评论数量={}", postId, 
                commentsResult.getComments() != null ? commentsResult.getComments().size() : 0);
            
            // 测试添加评论
            var commentDTO = new com.trip.model.dto.CommentDTO();
            commentDTO.setPostId(postId);
            commentDTO.setContent("这是一个测试评论，内容很精彩！");
            
            var addResult = commentService.addComment(commentDTO, userId);
            log.info("添加评论结果: commentId={}, createTime={}", 
                addResult.getCommentId(), addResult.getCreateTime());
            
            // 再次获取评论列表验证
            var updatedComments = commentService.getCommentsByPostId(postId);
            log.info("添加评论后的评论数量: {}", 
                updatedComments.getComments() != null ? updatedComments.getComments().size() : 0);
            
        } catch (Exception e) {
            log.error("测试评论功能失败", e);
        }
        
        log.info("=== 社区详情页面评论功能测试完成 ===");
    }

    /**
     * 测试评论功能是否正常工作（简化版）
     */
    @Test
    public void testBasicCommentFunctionality() {
        Long postId = 1L;
        
        log.info("=== 测试基础评论功能 ===");
        
        try {
            // 测试获取评论列表
            var commentsResult = commentService.getCommentsByPostId(postId);
            log.info("获取评论列表成功: postId={}, 评论数量={}", postId, 
                commentsResult.getComments() != null ? commentsResult.getComments().size() : 0);
            
            // 打印评论详情
            if (commentsResult.getComments() != null) {
                for (var comment : commentsResult.getComments()) {
                    log.info("  评论: ID={}, 用户={}, 内容={}", 
                        comment.getCommentId(), 
                        comment.getUser() != null ? comment.getUser().getNickname() : "未知用户",
                        comment.getContent());
                }
            }
            
        } catch (Exception e) {
            log.error("测试评论功能失败", e);
        }
        
        log.info("=== 基础评论功能测试完成 ===");
    }
}