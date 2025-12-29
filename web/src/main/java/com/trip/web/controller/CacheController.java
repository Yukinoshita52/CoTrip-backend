package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.web.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 缓存管理控制器
 * 用于调试和手动管理缓存
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final TripCacheService tripCacheService;
    private final CommunityFeedCacheService communityFeedCacheService;
    private final PostDetailCacheService postDetailCacheService;
    private final UserProfileCacheService userProfileCacheService;
    private final SearchCacheService searchCacheService;
    private final CommentsCacheService commentsCacheService;

    /**
     * 清除指定用户的行程缓存
     */
    @DeleteMapping("/trips/user/{userId}")
    public Result<String> evictUserTrips(@PathVariable Long userId) {
        tripCacheService.evictUserTrips(userId);
        return Result.ok("已清除用户行程缓存: userId=" + userId);
    }

    /**
     * 清除所有行程缓存
     */
    @DeleteMapping("/trips/all")
    public Result<String> evictAllTrips() {
        tripCacheService.evictAllTrips();
        return Result.ok("已清除所有行程缓存");
    }

    /**
     * 清除指定页面的社区动态缓存
     */
    @DeleteMapping("/community/feed")
    public Result<String> evictFeed(@RequestParam Integer page, @RequestParam Integer size) {
        communityFeedCacheService.evictFeed(page, size);
        return Result.ok("已清除社区动态缓存: page=" + page + ", size=" + size);
    }

    /**
     * 清除所有社区动态缓存
     */
    @DeleteMapping("/community/all")
    public Result<String> evictAllFeeds() {
        communityFeedCacheService.evictAllFeeds();
        return Result.ok("已清除所有社区动态缓存");
    }

    /**
     * 清除指定帖子详情缓存
     */
    @DeleteMapping("/post/{postId}/detail")
    public Result<String> evictPostDetail(@PathVariable Long postId) {
        postDetailCacheService.evictPostDetail(postId);
        return Result.ok("已清除帖子详情缓存: postId=" + postId);
    }

    /**
     * 清除所有帖子详情缓存
     */
    @DeleteMapping("/posts/details/all")
    public Result<String> evictAllPostDetails() {
        postDetailCacheService.evictAllPostDetails();
        return Result.ok("已清除所有帖子详情缓存");
    }

    /**
     * 清除指定用户资料缓存
     */
    @DeleteMapping("/user/{userId}/profile")
    public Result<String> evictUserProfile(@PathVariable Long userId) {
        userProfileCacheService.evictUserProfile(userId);
        return Result.ok("已清除用户资料缓存: userId=" + userId);
    }

    /**
     * 清除所有用户资料缓存
     */
    @DeleteMapping("/users/profiles/all")
    public Result<String> evictAllUserProfiles() {
        userProfileCacheService.evictAllUserProfiles();
        return Result.ok("已清除所有用户资料缓存");
    }

    /**
     * 清除所有搜索缓存
     */
    @DeleteMapping("/search/all")
    public Result<String> evictAllSearchCache() {
        searchCacheService.evictAllSearchCache();
        return Result.ok("已清除所有搜索缓存");
    }

    /**
     * 清除指定帖子的评论缓存
     */
    @DeleteMapping("/post/{postId}/comments")
    public Result<String> evictComments(@PathVariable Long postId) {
        commentsCacheService.evictComments(postId);
        return Result.ok("已清除评论缓存: postId=" + postId);
    }

    /**
     * 清除所有评论缓存
     */
    @DeleteMapping("/comments/all")
    public Result<String> evictAllComments() {
        commentsCacheService.evictAllComments();
        return Result.ok("已清除所有评论缓存");
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/all")
    public Result<String> evictAllCache() {
        tripCacheService.evictAllTrips();
        communityFeedCacheService.evictAllFeeds();
        postDetailCacheService.evictAllPostDetails();
        userProfileCacheService.evictAllUserProfiles();
        searchCacheService.evictAllSearchCache();
        commentsCacheService.evictAllComments();
        return Result.ok("已清除所有缓存");
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public Result<String> getCacheStats() {
        // 这里可以添加更详细的缓存统计信息
        return Result.ok("缓存统计功能待实现");
    }

    /**
     * 预热社区缓存
     */
    @PostMapping("/warmup/community")
    public Result<String> warmupCommunityCache() {
        log.info("开始预热社区缓存...");
        // 可以在这里预加载热门页面的缓存
        return Result.ok("社区缓存预热完成");
    }
}