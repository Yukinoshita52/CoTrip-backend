package com.trip.web.test;

import com.trip.model.vo.*;
import com.trip.web.service.CommunityService;
import com.trip.web.service.TripService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 缓存性能测试
 * 用于验证Redis缓存是否正常工作并提升性能
 */
@Slf4j
@SpringBootTest
public class CachePerformanceTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private CommunityService communityService;

    /**
     * 测试行程列表缓存性能
     */
    @Test
    public void testTripListCachePerformance() {
        Long userId = 5L; // 使用一个存在的用户ID
        
        log.info("=== 测试行程列表缓存性能 ===");
        
        // 第一次调用（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        List<TripVO> trips1 = tripService.getUserTrips(userId);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;
        
        log.info("第一次调用（缓存未命中）: 耗时{}ms, 结果数量: {}", duration1, trips1.size());
        
        // 第二次调用（缓存命中）
        long startTime2 = System.currentTimeMillis();
        List<TripVO> trips2 = tripService.getUserTrips(userId);
        long endTime2 = System.currentTimeMillis();
        long duration2 = endTime2 - startTime2;
        
        log.info("第二次调用（缓存命中）: 耗时{}ms, 结果数量: {}", duration2, trips2.size());
        
        // 计算性能提升
        if (duration1 > 0) {
            double improvement = ((double)(duration1 - duration2) / duration1) * 100;
            log.info("性能提升: {:.2f}%", improvement);
        }
        
        // 验证结果一致性
        if (trips1.size() == trips2.size()) {
            log.info("✅ 缓存结果一致性验证通过");
        } else {
            log.error("❌ 缓存结果不一致: 第一次{}, 第二次{}", trips1.size(), trips2.size());
        }
    }

    /**
     * 测试社区动态缓存性能
     */
    @Test
    public void testCommunityFeedCachePerformance() {
        Integer page = 1;
        Integer size = 10;
        
        log.info("=== 测试社区动态缓存性能 ===");
        
        // 第一次调用（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        FeedPageVO feed1 = communityService.getFeed(page, size);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;
        
        log.info("第一次调用（缓存未命中）: 耗时{}ms, 结果数量: {}", duration1, feed1.getList().size());
        
        // 第二次调用（缓存命中）
        long startTime2 = System.currentTimeMillis();
        FeedPageVO feed2 = communityService.getFeed(page, size);
        long endTime2 = System.currentTimeMillis();
        long duration2 = endTime2 - startTime2;
        
        log.info("第二次调用（缓存命中）: 耗时{}ms, 结果数量: {}", duration2, feed2.getList().size());
        
        // 计算性能提升
        if (duration1 > 0) {
            double improvement = ((double)(duration1 - duration2) / duration1) * 100;
            log.info("性能提升: {:.2f}%", improvement);
        }
        
        // 验证结果一致性
        if (feed1.getList().size() == feed2.getList().size()) {
            log.info("✅ 缓存结果一致性验证通过");
        } else {
            log.error("❌ 缓存结果不一致: 第一次{}, 第二次{}", feed1.getList().size(), feed2.getList().size());
        }
    }

    /**
     * 测试帖子详情缓存性能
     */
    @Test
    public void testPostDetailCachePerformance() {
        Long postId = 1L; // 使用一个存在的帖子ID
        
        log.info("=== 测试帖子详情缓存性能 ===");
        
        // 第一次调用（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        PostDetailVO detail1 = communityService.getPostDetail(postId);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;
        
        log.info("第一次调用（缓存未命中）: 耗时{}ms, 帖子存在: {}", duration1, detail1 != null);
        
        if (detail1 != null) {
            // 第二次调用（缓存命中）
            long startTime2 = System.currentTimeMillis();
            PostDetailVO detail2 = communityService.getPostDetail(postId);
            long endTime2 = System.currentTimeMillis();
            long duration2 = endTime2 - startTime2;
            
            log.info("第二次调用（缓存命中）: 耗时{}ms", duration2);
            
            // 计算性能提升
            if (duration1 > 0) {
                double improvement = ((double)(duration1 - duration2) / duration1) * 100;
                log.info("性能提升: {:.2f}%", improvement);
            }
            
            log.info("✅ 帖子详情缓存测试完成");
        } else {
            log.warn("⚠️ 帖子不存在，跳过缓存测试");
        }
    }

    /**
     * 测试用户资料缓存性能
     */
    @Test
    public void testUserProfileCachePerformance() {
        Long userId = 5L; // 使用一个存在的用户ID
        
        log.info("=== 测试用户资料缓存性能 ===");
        
        // 第一次调用（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        UserProfileVO profile1 = communityService.getUserProfile(userId);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;
        
        log.info("第一次调用（缓存未命中）: 耗时{}ms, 用户: {}", duration1, 
            profile1 != null ? profile1.getNickname() : "null");
        
        if (profile1 != null) {
            // 第二次调用（缓存命中）
            long startTime2 = System.currentTimeMillis();
            UserProfileVO profile2 = communityService.getUserProfile(userId);
            long endTime2 = System.currentTimeMillis();
            long duration2 = endTime2 - startTime2;
            
            log.info("第二次调用（缓存命中）: 耗时{}ms", duration2);
            
            // 计算性能提升
            if (duration1 > 0) {
                double improvement = ((double)(duration1 - duration2) / duration1) * 100;
                log.info("性能提升: {:.2f}%", improvement);
            }
            
            log.info("✅ 用户资料缓存测试完成");
        }
    }

    /**
     * 测试搜索缓存性能
     */
    @Test
    public void testSearchCachePerformance() {
        String keyword = "旅行"; // 使用一个常见的搜索关键词
        
        log.info("=== 测试搜索缓存性能 ===");
        
        // 第一次调用（缓存未命中）
        long startTime1 = System.currentTimeMillis();
        SearchPostVO search1 = communityService.searchMatchPosts(keyword);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;
        
        log.info("第一次调用（缓存未命中）: 耗时{}ms, 结果数量: {}", duration1, search1.getResults().size());
        
        // 第二次调用（缓存命中）
        long startTime2 = System.currentTimeMillis();
        SearchPostVO search2 = communityService.searchMatchPosts(keyword);
        long endTime2 = System.currentTimeMillis();
        long duration2 = endTime2 - startTime2;
        
        log.info("第二次调用（缓存命中）: 耗时{}ms, 结果数量: {}", duration2, search2.getResults().size());
        
        // 计算性能提升
        if (duration1 > 0) {
            double improvement = ((double)(duration1 - duration2) / duration1) * 100;
            log.info("性能提升: {:.2f}%", improvement);
        }
        
        // 验证结果一致性
        if (search1.getResults().size() == search2.getResults().size()) {
            log.info("✅ 搜索缓存结果一致性验证通过");
        } else {
            log.error("❌ 搜索缓存结果不一致: 第一次{}, 第二次{}", 
                search1.getResults().size(), search2.getResults().size());
        }
    }

    /**
     * 测试多次调用的平均性能
     */
    @Test
    public void testAveragePerformance() {
        Long userId = 5L;
        int iterations = 5;
        
        log.info("=== 测试多次调用平均性能 ===");
        
        // 预热缓存
        tripService.getUserTrips(userId);
        
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            List<TripVO> trips = tripService.getUserTrips(userId);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            log.info("第{}次调用: 耗时{}ms, 结果数量: {}", i + 1, duration, trips.size());
        }
        
        double averageTime = (double) totalTime / iterations;
        log.info("平均耗时: {:.2f}ms", averageTime);
        
        if (averageTime < 50) { // 如果平均耗时小于50ms，认为缓存效果良好
            log.info("✅ 缓存性能良好");
        } else {
            log.warn("⚠️ 缓存性能可能需要优化");
        }
    }

    /**
     * 综合性能测试
     */
    @Test
    public void testComprehensivePerformance() {
        log.info("=== 综合性能测试 ===");
        
        // 测试各种操作的性能
        long totalStartTime = System.currentTimeMillis();
        
        // 1. 行程列表
        List<TripVO> trips = tripService.getUserTrips(5L);
        log.info("行程列表查询完成: {} 个行程", trips.size());
        
        // 2. 社区动态
        FeedPageVO feed = communityService.getFeed(1, 10);
        log.info("社区动态查询完成: {} 个帖子", feed.getList().size());
        
        // 3. 用户资料
        UserProfileVO profile = communityService.getUserProfile(5L);
        log.info("用户资料查询完成: {}", profile != null ? profile.getNickname() : "null");
        
        // 4. 搜索
        SearchPostVO search = communityService.searchMatchPosts("旅行");
        log.info("搜索查询完成: {} 个结果", search.getResults().size());
        
        long totalEndTime = System.currentTimeMillis();
        long totalDuration = totalEndTime - totalStartTime;
        
        log.info("综合测试总耗时: {}ms", totalDuration);
        
        if (totalDuration < 500) { // 如果总耗时小于500ms，认为性能良好
            log.info("✅ 综合性能良好");
        } else {
            log.warn("⚠️ 综合性能可能需要优化");
        }
    }
}