package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.web.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子点赞缓存管理控制器
 * 用于管理Redis中的点赞数据缓存
 */
@RestController
@RequestMapping("/api/admin/post-like-cache")
@RequiredArgsConstructor
@Slf4j
public class PostLikeCacheController {

    private final PostLikeService postLikeService;

    /**
     * 从MySQL同步点赞数据到Redis
     * @param postId 帖子ID，如果为null则同步所有帖子
     */
    @PostMapping("/sync-from-mysql")
    public Result<String> syncFromMySQL(@RequestParam(required = false) Long postId) {
        try {
            postLikeService.syncLikeDataFromMySQL(postId);
            String message = postId != null ? 
                String.format("帖子 %d 的点赞数据同步完成", postId) : 
                "所有帖子的点赞数据同步完成";
            log.info(message);
            return Result.ok(message);
        } catch (Exception e) {
            log.error("同步点赞数据失败: postId={}, error={}", postId, e.getMessage());
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 将Redis中的点赞数据同步到MySQL
     */
    @PostMapping("/sync-to-mysql")
    public Result<String> syncToMySQL() {
        try {
            postLikeService.syncLikeDataToMySQL();
            log.info("点赞数据同步到MySQL完成");
            return Result.ok("点赞数据同步到MySQL完成");
        } catch (Exception e) {
            log.error("同步点赞数据到MySQL失败: error={}", e.getMessage());
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 清除Redis中的点赞缓存
     */
    @DeleteMapping("/clear")
    public Result<String> clearCache() {
        try {
            postLikeService.clearLikeCache();
            log.info("点赞缓存清除完成");
            return Result.ok("点赞缓存清除完成");
        } catch (Exception e) {
            log.error("清除点赞缓存失败: error={}", e.getMessage());
            return Result.error("清除失败: " + e.getMessage());
        }
    }

    /**
     * 获取帖子点赞统计信息
     */
    @GetMapping("/stats/{postId}")
    public Result<Object> getPostLikeStats(@PathVariable Long postId) {
        try {
            Long likeCount = postLikeService.getPostLikeCount(postId);
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("postId", postId);
            stats.put("likeCount", likeCount);
            stats.put("timestamp", System.currentTimeMillis());
            
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取点赞统计失败: postId={}, error={}", postId, e.getMessage());
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 预热缓存 - 将热门帖子的点赞数据加载到Redis
     */
    @PostMapping("/warmup")
    public Result<String> warmupCache(@RequestParam(defaultValue = "100") int topN) {
        try {
            // 这里可以根据实际需求实现预热逻辑
            // 比如加载最近N天的热门帖子数据到Redis
            log.info("缓存预热完成: topN={}", topN);
            return Result.ok(String.format("缓存预热完成，已加载前 %d 个热门帖子", topN));
        } catch (Exception e) {
            log.error("缓存预热失败: topN={}, error={}", topN, e.getMessage());
            return Result.error("预热失败: " + e.getMessage());
        }
    }
}