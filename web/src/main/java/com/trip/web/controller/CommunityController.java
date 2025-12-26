package com.trip.web.controller;

import com.trip.common.login.LoginUser;
import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.model.dto.CommentDTO;
import com.trip.model.dto.TripDTO;
import com.trip.model.entity.Post;
import com.trip.model.entity.Trip;
import com.trip.model.vo.*;
import com.trip.web.mapper.CommunityMapper;
import com.trip.web.service.CommentService;
import com.trip.web.service.CommunityService;
import com.trip.web.service.PostService;
import com.trip.web.service.PostViewService;
import com.trip.web.service.PostLikeService;
import com.trip.web.service.TripService;
import com.trip.web.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @Autowired
    private PostViewService postViewService;
    @Autowired
    private PostLikeService postLikeService;
    @Autowired
    private TripService tripService;

    // 1. 内容流 Feed
    @GetMapping("/feed")
    public Result<FeedPageVO> getFeed(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(communityService.getFeed(page, size));
    }

    // 2. 帖子详情
    @GetMapping("/post/{postId}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        // 增加浏览量
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        Long userId = loginUser != null ? loginUser.getUserId() : null;
        Long viewCount = postViewService.incrementViewCount(postId, userId);
        
        PostDetailVO vo = communityService.getPostDetail(postId);
        // 更新浏览量到返回结果中
        if (vo != null) {
            if (vo.getStats() != null) {
                vo.getStats().setViewCount(viewCount.intValue());
            } else {
                // 如果stats为null，创建一个新的StatVO
                com.trip.model.vo.StatVO stats = new com.trip.model.vo.StatVO();
                stats.setViewCount(viewCount.intValue());
                stats.setLikeCount(0);
                stats.setCommentCount(0);
                vo.setStats(stats);
            }
        }
        return Result.ok(vo);
    }

    // 3. 发布帖子
    @PostMapping("/post")
    public Result<PostCreatedVO> createPost(@RequestBody TripDTO dto) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        PostCreatedVO vo = communityService.createPost(loginUser.getUserId(),dto);
        return Result.ok(vo);
    }

    // 4. 删除帖子
    @DeleteMapping("/post/{postId}")
    public Result<PostDeletedVO> deletePost(@PathVariable Long postId) {
        postService.removeById(postId);
        return Result.ok();
    }

    // 4.1 更新帖子
    @PutMapping("/post/{postId}")
    public Result<Void> updatePost(@PathVariable Long postId, @RequestBody TripDTO dto) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        // 检查帖子是否存在且属于当前用户
        Post post = postService.getById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        
        if (!post.getUserId().equals(loginUser.getUserId())) {
            return Result.error("无权限编辑此帖子");
        }
        
        // 更新关联的行程信息（帖子的标题和描述实际存储在trip表中）
        Trip trip = tripService.getById(post.getTripId());
        if (trip == null) {
            return Result.error("关联的行程不存在");
        }
        
        // 更新行程的名称和描述
        trip.setName(dto.getName());
        trip.setDescription(dto.getDescription());
        tripService.updateById(trip);
        
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

    // 5.3 删除评论（具体看删除的是父评论还是子评论）
    @DeleteMapping("/comment/{commentId}")
    public Result<CommentDeletedVO> deleteComment(@PathVariable Long commentId) {
        CommentDeletedVO res = commentService.deleteComment(commentId);
        return Result.ok(res);
    }

    // 6.1 点赞
    @PostMapping("/post/{postId}/like")
    public Result<PostLikeVO> likePost(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        PostLikeVO res = postLikeService.likePost(postId, loginUser.getUserId());
        return Result.ok(res);
    }

    // 6.2 取消点赞
    @DeleteMapping("/post/{postId}/like")
    public Result<PostLikeVO> unlikePost(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        PostLikeVO res = postLikeService.unlikePost(postId, loginUser.getUserId());
        return Result.ok(res);
    }

    // 6.3 查询点赞用户
    @GetMapping("/post/{postId}/likes")
    public Result<PostLikeUsersVO> getPostLikes(@PathVariable Long postId) {
        PostLikeUsersVO res = commentService.getPostLikeUsers(postId);
        return Result.ok(res);
    }

    // 6.4 检查用户是否已点赞
    @GetMapping("/post/{postId}/like/status")
    public Result<Boolean> checkLikeStatus(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        boolean isLiked = postLikeService.isPostLikedByUser(postId, loginUser.getUserId());
        return Result.ok(isLiked);
    }

    // 6.5 获取帖子点赞数
    @GetMapping("/post/{postId}/like/count")
    public Result<Long> getPostLikeCount(@PathVariable Long postId) {
        Long likeCount = postLikeService.getPostLikeCount(postId);
        return Result.ok(likeCount);
    }

    // 6.6 获取帖子浏览量
    @GetMapping("/post/{postId}/views")
    public Result<Long> getPostViews(@PathVariable Long postId) {
        Long viewCount = postViewService.getViewCount(postId);
        return Result.ok(viewCount);
    }

    // 6.7 获取帖子统计信息
    @GetMapping("/post/{postId}/stats")
    public Result<StatVO> getPostStats(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        Long userId = loginUser != null ? loginUser.getUserId() : null;
        
        // 获取基本统计信息
        StatVO stats = postService.getById(postId) != null ? 
            communityService.getPostStats(postId, userId) : null;
        
        if (stats != null) {
            // 添加浏览量和点赞数（从Redis获取）
            Long viewCount = postViewService.getViewCount(postId);
            Long likeCount = postLikeService.getPostLikeCount(postId);
            stats.setViewCount(viewCount.intValue());
            stats.setLikeCount(likeCount.intValue());
        }
        
        return Result.ok(stats);
    }

    // 7. 用户主页
    @GetMapping("/user/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        UserProfileVO res = communityService.getUserProfile(userId);
        return Result.ok(res);
    }

    /**
     * 8.1 搜索帖子
     * 这个接口先简单实现（keyword就直接对应去查trip.name）
     * 后续优化思路：看看keyword是否是region名、是否跟description相关等
     * @param keyword
     * @return
     */
    @GetMapping("/search")
    public Result<SearchPostVO> searchPosts(@RequestParam String keyword) {
        SearchPostVO res = communityService.searchMatchPosts(keyword);
        return Result.ok(res);
    }

    // 8.2 搜索用户
    @GetMapping("/search/user")
    public Result<SearchUserVO> searchUsers(@RequestParam String keyword) {
        SearchUserVO res = communityService.searchAuthorByKeyword(keyword);
        return Result.ok(res);
    }

    // 8.3 获取当前用户已分享的行程ID列表
    @GetMapping("/my-shared-trips")
    public Result<List<Long>> getMySharedTripIds() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        Long userId = loginUser.getUserId();
        System.out.println("获取用户 " + userId + " 的已分享行程ID");
        
        List<Long> sharedTripIds = communityService.getUserSharedTripIds(userId);
        System.out.println("返回已分享行程ID: " + sharedTripIds);
        
        return Result.ok(sharedTripIds);
    }

    // 9.1 收藏帖子
    @PostMapping("/post/{postId}/collect")
    public Result<Void> collectPost(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        // TODO: 实现收藏功能
        // 这里可以创建一个收藏表来存储用户收藏的帖子
        return Result.ok();
    }

    // 9.2 取消收藏帖子
    @DeleteMapping("/post/{postId}/collect")
    public Result<Void> uncollectPost(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        // TODO: 实现取消收藏功能
        return Result.ok();
    }

    // 9.3 举报帖子
    @PostMapping("/post/{postId}/report")
    public Result<Void> reportPost(@PathVariable Long postId, @RequestBody Map<String, String> request) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("举报原因不能为空");
        }
        
        // TODO: 实现举报功能
        // 这里可以创建一个举报表来存储举报信息
        return Result.ok();
    }

    // 临时调试接口：获取用户行程数据结构
    @GetMapping("/debug/my-trips")
    public Result<Object> debugMyTrips() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }
        
        // 这里需要调用 TripService 来获取用户的行程
        // 由于我们没有直接的 TripService 引用，我们返回一个提示
        return Result.ok("请检查 /api/trips 接口返回的数据结构");
    }
}
