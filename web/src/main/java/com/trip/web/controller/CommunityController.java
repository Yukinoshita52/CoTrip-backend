package com.trip.web.controller;

import com.trip.common.login.LoginUser;
import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.model.dto.CommentDTO;
import com.trip.model.dto.TripDTO;
import com.trip.model.vo.*;
import com.trip.web.service.CommentService;
import com.trip.web.service.CommunityService;
import com.trip.web.service.PostService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: CommunityController
 * Package: com.trip.web.controller
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 19:58
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    @Resource
    private CommunityService communityService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    // 1. 内容流 Feed
    @GetMapping("/feed")
    public Result<FeedPageVO> getFeed(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(communityService.getFeed(page, size));
    }

    // 2. 帖子详情
    @GetMapping("/post/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        PostDetailVO vo = communityService.getPostDetail(postId);
        return Result.ok(vo);
    }

    // 3. 发布帖子
    @PostMapping("/post")
    public Result<PostCreatedVO> createPost(@RequestBody TripDTO dto) {
        PostCreatedVO vo = communityService.createPost(dto);
        return Result.ok(vo);
    }

    // 4. 删除帖子
    @DeleteMapping("/post/{postId}")
    public Result<PostDeletedVO> deletePost(@PathVariable Long postId) {
        postService.removeById(postId);
        return Result.ok();
    }

    // 5.1 获取评论列表
    @GetMapping("/post/{postId}/comments")
    public Result<CommentListVO> getComments(@PathVariable Long postId) {
        CommentListVO res = commentService.getCommentsByPostId(postId);
        return Result.ok(res);
    }

    // 5.2 新增评论
    @PostMapping("/comment")
    public Result<CommentCreatedVO> addComment(@RequestBody CommentDTO dto) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        CommentCreatedVO res = commentService.addComment(dto,loginUser.getUserId());
        return Result.ok(res);
    }

    // 5.3 删除评论
    @DeleteMapping("/comment/{commentId}")
    public Result<CommentDeletedVO> deleteComment(@PathVariable Long commentId) {
        return null;
    }

    // 6.1 点赞
    @PostMapping("/post/{postId}/like")
    public Result<PostLikeVO> likePost(@PathVariable Long postId) {
        return null;
    }

    // 6.2 取消点赞
    @DeleteMapping("/post/{postId}/like")
    public Result<PostLikeVO> unlikePost(@PathVariable Long postId) {
        return null;
    }

    // 6.3 查询点赞用户
    @GetMapping("/post/{postId}/likes")
    public Result<PostLikeUsersVO> getPostLikes(@PathVariable Long postId) {
        return null;
    }

    // 7. 用户主页
    @GetMapping("/user/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return null;
    }

    // 8.1 搜索帖子
    @GetMapping("/search")
    public Result<SearchPostVO> searchPosts(@RequestParam String keyword) {
        return null;
    }

    // 8.2 搜索用户
    @GetMapping("/search/user")
    public Result<SearchUserVO> searchUsers(@RequestParam String keyword) {
        return null;
    }
}
