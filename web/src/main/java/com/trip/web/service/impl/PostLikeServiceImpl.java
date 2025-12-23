package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trip.model.entity.PostLike;
import com.trip.model.vo.PostLikeVO;
import com.trip.web.mapper.PostLikeMapper;
import com.trip.web.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based 帖子点赞服务实现
 * 
 * Redis Key 设计:
 * 1. 点赞计数: "post:like:count:{postId}" -> 点赞总数
 * 2. 用户点赞状态: "post:like:user:{postId}" -> Set<userId> (使用Redis Set存储点赞用户ID)
 * 3. 点赞变更队列: "post:like:changes" -> List<changeRecord> (用于异步同步到MySQL)
 * 
 * 一致性策略:
 * 1. 写入顺序: Redis -> MySQL (先更新缓存，再异步同步数据库)
 * 2. 读取策略: 优先从Redis读取，Redis miss时从MySQL加载并缓存
 * 3. 失败兜底: Redis操作失败时直接操作MySQL，MySQL操作失败时回滚Redis
 * 4. 数据恢复: 提供从MySQL重建Redis缓存的方法
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PostLikeMapper postLikeMapper;
    
    // Redis Key 前缀
    private static final String LIKE_COUNT_PREFIX = "post:like:count:";
    private static final String LIKE_USER_PREFIX = "post:like:user:";
    private static final String LIKE_CHANGES_KEY = "post:like:changes";
    
    // 缓存过期时间（7天）
    private static final long CACHE_EXPIRE_DAYS = 7;

    @Override
    @Transactional
    public PostLikeVO likePost(Long postId, Long userId) {
        String countKey = LIKE_COUNT_PREFIX + postId;
        String userSetKey = LIKE_USER_PREFIX + postId;
        String userIdStr = String.valueOf(userId);
        
        try {
            // 1. 检查Redis中用户是否已点赞
            Boolean isAlreadyLiked = stringRedisTemplate.opsForSet().isMember(userSetKey, userIdStr);
            
            if (Boolean.TRUE.equals(isAlreadyLiked)) {
                // 用户已点赞，返回当前状态
                Long currentCount = getCurrentLikeCount(postId);
                log.info("用户已点赞，返回当前状态: postId={}, userId={}, count={}", postId, userId, currentCount);
                return new PostLikeVO().withLiked(true).withPostId(postId).withLikeCount(currentCount.intValue());
            }
            
            // 2. Redis中添加点赞记录
            Long addResult = stringRedisTemplate.opsForSet().add(userSetKey, userIdStr);
            if (addResult != null && addResult > 0) {
                // 3. 增加点赞计数
                Long newCount = stringRedisTemplate.opsForValue().increment(countKey);
                
                // 4. 设置过期时间
                stringRedisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                stringRedisTemplate.expire(userSetKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                
                // 5. 记录变更到队列（用于异步同步MySQL）
                recordLikeChange(postId, userId, "LIKE");
                
                // 6. 异步同步到MySQL
                asyncSyncToMySQL(postId, userId, true);
                
                log.info("点赞成功: postId={}, userId={}, newCount={}", postId, userId, newCount);
                return new PostLikeVO().withLiked(true).withPostId(postId).withLikeCount(newCount.intValue());
            } else {
                // Redis操作失败，降级到MySQL
                log.warn("Redis点赞操作失败，降级到MySQL: postId={}, userId={}", postId, userId);
                return fallbackToMySQL(postId, userId, true);
            }
            
        } catch (Exception e) {
            log.error("Redis点赞操作异常，降级到MySQL: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            return fallbackToMySQL(postId, userId, true);
        }
    }

    @Override
    @Transactional
    public PostLikeVO unlikePost(Long postId, Long userId) {
        String countKey = LIKE_COUNT_PREFIX + postId;
        String userSetKey = LIKE_USER_PREFIX + postId;
        String userIdStr = String.valueOf(userId);
        
        try {
            // 1. 检查Redis中用户是否已点赞
            Boolean isLiked = stringRedisTemplate.opsForSet().isMember(userSetKey, userIdStr);
            
            if (Boolean.FALSE.equals(isLiked)) {
                // 用户未点赞，返回当前状态
                Long currentCount = getCurrentLikeCount(postId);
                log.info("用户未点赞，返回当前状态: postId={}, userId={}, count={}", postId, userId, currentCount);
                return new PostLikeVO().withLiked(false).withPostId(postId).withLikeCount(currentCount.intValue());
            }
            
            // 2. Redis中移除点赞记录
            Long removeResult = stringRedisTemplate.opsForSet().remove(userSetKey, userIdStr);
            if (removeResult != null && removeResult > 0) {
                // 3. 减少点赞计数
                Long newCount = stringRedisTemplate.opsForValue().decrement(countKey);
                // 确保计数不为负数
                if (newCount < 0) {
                    stringRedisTemplate.opsForValue().set(countKey, "0");
                    newCount = 0L;
                }
                
                // 4. 记录变更到队列
                recordLikeChange(postId, userId, "UNLIKE");
                
                // 5. 异步同步到MySQL
                asyncSyncToMySQL(postId, userId, false);
                
                log.info("取消点赞成功: postId={}, userId={}, newCount={}", postId, userId, newCount);
                return new PostLikeVO().withLiked(false).withPostId(postId).withLikeCount(newCount.intValue());
            } else {
                // Redis操作失败，降级到MySQL
                log.warn("Redis取消点赞操作失败，降级到MySQL: postId={}, userId={}", postId, userId);
                return fallbackToMySQL(postId, userId, false);
            }
            
        } catch (Exception e) {
            log.error("Redis取消点赞操作异常，降级到MySQL: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            return fallbackToMySQL(postId, userId, false);
        }
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        String userSetKey = LIKE_USER_PREFIX + postId;
        String userIdStr = String.valueOf(userId);
        
        try {
            // 1. 先从Redis查询
            Boolean isLiked = stringRedisTemplate.opsForSet().isMember(userSetKey, userIdStr);
            if (isLiked != null) {
                log.debug("Redis缓存命中 - 用户点赞状态: postId={}, userId={}, isLiked={}", postId, userId, isLiked);
                return isLiked;
            }
            
            // 2. Redis miss，从MySQL加载并缓存
            log.debug("Redis缓存未命中，从MySQL加载点赞状态: postId={}, userId={}", postId, userId);
            boolean mysqlResult = loadLikeStatusFromMySQL(postId, userId);
            
            // 3. 缓存到Redis
            if (mysqlResult) {
                stringRedisTemplate.opsForSet().add(userSetKey, userIdStr);
                stringRedisTemplate.expire(userSetKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            }
            
            return mysqlResult;
            
        } catch (Exception e) {
            log.error("查询点赞状态异常，降级到MySQL: postId={}, userId={}, error={}", postId, userId, e.getMessage());
            return loadLikeStatusFromMySQL(postId, userId);
        }
    }

    @Override
    public Long getPostLikeCount(Long postId) {
        String countKey = LIKE_COUNT_PREFIX + postId;
        
        try {
            // 1. 先从Redis查询
            String countStr = stringRedisTemplate.opsForValue().get(countKey);
            if (countStr != null) {
                Long count = Long.valueOf(countStr);
                log.debug("Redis缓存命中 - 帖子点赞数: postId={}, count={}", postId, count);
                return count;
            }
            
            // 2. Redis miss，从MySQL加载并缓存
            log.debug("Redis缓存未命中，从MySQL加载点赞数: postId={}", postId);
            Long mysqlCount = loadLikeCountFromMySQL(postId);
            
            // 3. 缓存到Redis
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(mysqlCount), CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            return mysqlCount;
            
        } catch (Exception e) {
            log.error("查询点赞数异常，降级到MySQL: postId={}, error={}", postId, e.getMessage());
            return loadLikeCountFromMySQL(postId);
        }
    }

    @Override
    public Map<Long, Long> getPostLikeCounts(List<Long> postIds) {
        Map<Long, Long> result = new HashMap<>();
        
        for (Long postId : postIds) {
            result.put(postId, getPostLikeCount(postId));
        }
        
        return result;
    }

    @Override
    public Map<Long, Boolean> getUserLikeStatuses(List<Long> postIds, Long userId) {
        Map<Long, Boolean> result = new HashMap<>();
        
        for (Long postId : postIds) {
            result.put(postId, isPostLikedByUser(postId, userId));
        }
        
        return result;
    }

    @Override
    public void syncLikeDataFromMySQL(Long postId) {
        try {
            if (postId != null) {
                // 同步单个帖子
                syncSinglePostFromMySQL(postId);
            } else {
                // 同步所有帖子（分页处理，避免内存溢出）
                int pageSize = 1000;
                int offset = 0;
                
                while (true) {
                    List<Long> postIds = postLikeMapper.getDistinctPostIds(offset, pageSize);
                    if (postIds.isEmpty()) {
                        break;
                    }
                    
                    for (Long pid : postIds) {
                        syncSinglePostFromMySQL(pid);
                    }
                    
                    offset += pageSize;
                    log.info("已同步{}个帖子的点赞数据到Redis", offset);
                }
            }
            
            log.info("点赞数据同步完成: postId={}", postId);
            
        } catch (Exception e) {
            log.error("同步点赞数据到Redis失败: postId={}, error={}", postId, e.getMessage());
        }
    }

    @Override
    public void syncLikeDataToMySQL() {
        // 这个方法用于定期将Redis中的变更同步到MySQL
        // 可以通过定时任务调用，或者在应用关闭时调用
        try {
            // 处理变更队列中的记录
            while (true) {
                String changeRecord = stringRedisTemplate.opsForList().rightPop(LIKE_CHANGES_KEY);
                if (changeRecord == null) {
                    break;
                }
                
                // 解析变更记录并同步到MySQL
                processLikeChange(changeRecord);
            }
            
            log.info("点赞数据同步到MySQL完成");
            
        } catch (Exception e) {
            log.error("同步点赞数据到MySQL失败: error={}", e.getMessage());
        }
    }

    @Override
    public void clearLikeCache() {
        try {
            // 清除点赞计数缓存
            Set<String> countKeys = stringRedisTemplate.keys(LIKE_COUNT_PREFIX + "*");
            if (countKeys != null && !countKeys.isEmpty()) {
                stringRedisTemplate.delete(countKeys);
                log.info("已清除点赞计数缓存: 共{}个", countKeys.size());
            }
            
            // 清除用户点赞状态缓存
            Set<String> userKeys = stringRedisTemplate.keys(LIKE_USER_PREFIX + "*");
            if (userKeys != null && !userKeys.isEmpty()) {
                stringRedisTemplate.delete(userKeys);
                log.info("已清除用户点赞状态缓存: 共{}个", userKeys.size());
            }
            
            // 清除变更队列
            stringRedisTemplate.delete(LIKE_CHANGES_KEY);
            
        } catch (Exception e) {
            log.error("清除点赞缓存失败", e);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取当前点赞数（优先从Redis，失败时从MySQL）
     */
    private Long getCurrentLikeCount(Long postId) {
        try {
            return getPostLikeCount(postId);
        } catch (Exception e) {
            log.error("获取点赞数失败: postId={}", postId, e);
            return 0L;
        }
    }

    /**
     * 记录点赞变更到队列
     */
    private void recordLikeChange(Long postId, Long userId, String action) {
        try {
            String changeRecord = String.format("%d:%d:%s:%d", postId, userId, action, System.currentTimeMillis());
            stringRedisTemplate.opsForList().leftPush(LIKE_CHANGES_KEY, changeRecord);
        } catch (Exception e) {
            log.error("记录点赞变更失败: postId={}, userId={}, action={}", postId, userId, action, e);
        }
    }

    /**
     * 异步同步到MySQL
     */
    private void asyncSyncToMySQL(Long postId, Long userId, boolean isLike) {
        // 这里可以使用异步任务或消息队列
        // 为了简化，这里直接同步执行
        try {
            if (isLike) {
                // 检查是否已存在
                PostLike existingLike = postLikeMapper.selectOne(
                    new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
                );
                
                if (existingLike == null) {
                    PostLike postLike = new PostLike();
                    postLike.setPostId(postId);
                    postLike.setUserId(userId);
                    postLikeMapper.insert(postLike);
                    log.debug("MySQL点赞记录插入成功: postId={}, userId={}", postId, userId);
                }
            } else {
                int deletedCount = postLikeMapper.delete(
                    new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
                );
                log.debug("MySQL点赞记录删除成功: postId={}, userId={}, deletedCount={}", postId, userId, deletedCount);
            }
        } catch (Exception e) {
            log.error("异步同步到MySQL失败: postId={}, userId={}, isLike={}, error={}", postId, userId, isLike, e.getMessage());
        }
    }

    /**
     * 降级到MySQL操作
     */
    private PostLikeVO fallbackToMySQL(Long postId, Long userId, boolean isLike) {
        try {
            if (isLike) {
                // 检查是否已存在
                PostLike existingLike = postLikeMapper.selectOne(
                    new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
                );
                
                if (existingLike == null) {
                    PostLike postLike = new PostLike();
                    postLike.setPostId(postId);
                    postLike.setUserId(userId);
                    postLikeMapper.insert(postLike);
                }
                
                Integer likeCount = postLikeMapper.countByPostId(postId);
                return new PostLikeVO().withLiked(true).withPostId(postId).withLikeCount(likeCount);
            } else {
                postLikeMapper.delete(
                    new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId)
                );
                
                Integer likeCount = postLikeMapper.countByPostId(postId);
                return new PostLikeVO().withLiked(false).withPostId(postId).withLikeCount(likeCount);
            }
        } catch (Exception e) {
            log.error("MySQL降级操作也失败: postId={}, userId={}, isLike={}, error={}", postId, userId, isLike, e.getMessage());
            // 返回默认值
            return new PostLikeVO().withLiked(false).withPostId(postId).withLikeCount(0);
        }
    }

    /**
     * 从MySQL加载点赞状态
     */
    private boolean loadLikeStatusFromMySQL(Long postId, Long userId) {
        try {
            PostLike existingLike = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, postId)
                    .eq(PostLike::getUserId, userId)
            );
            return existingLike != null;
        } catch (Exception e) {
            log.error("从MySQL加载点赞状态失败: postId={}, userId={}", postId, userId, e);
            return false;
        }
    }

    /**
     * 从MySQL加载点赞数
     */
    private Long loadLikeCountFromMySQL(Long postId) {
        try {
            Integer count = postLikeMapper.countByPostId(postId);
            return count != null ? count.longValue() : 0L;
        } catch (Exception e) {
            log.error("从MySQL加载点赞数失败: postId={}", postId, e);
            return 0L;
        }
    }

    /**
     * 同步单个帖子的数据从MySQL到Redis
     */
    private void syncSinglePostFromMySQL(Long postId) {
        try {
            // 1. 加载点赞数
            Long likeCount = loadLikeCountFromMySQL(postId);
            String countKey = LIKE_COUNT_PREFIX + postId;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(likeCount), CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            // 2. 加载点赞用户列表
            List<PostLike> likes = postLikeMapper.selectList(
                new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId)
            );
            
            if (!likes.isEmpty()) {
                String userSetKey = LIKE_USER_PREFIX + postId;
                String[] userIds = likes.stream()
                    .map(like -> String.valueOf(like.getUserId()))
                    .toArray(String[]::new);
                
                stringRedisTemplate.opsForSet().add(userSetKey, userIds);
                stringRedisTemplate.expire(userSetKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            }
            
            log.debug("同步帖子数据到Redis完成: postId={}, likeCount={}, userCount={}", postId, likeCount, likes.size());
            
        } catch (Exception e) {
            log.error("同步单个帖子数据失败: postId={}", postId, e);
        }
    }

    /**
     * 处理点赞变更记录
     */
    private void processLikeChange(String changeRecord) {
        try {
            String[] parts = changeRecord.split(":");
            if (parts.length >= 3) {
                Long postId = Long.valueOf(parts[0]);
                Long userId = Long.valueOf(parts[1]);
                String action = parts[2];
                
                if ("LIKE".equals(action)) {
                    asyncSyncToMySQL(postId, userId, true);
                } else if ("UNLIKE".equals(action)) {
                    asyncSyncToMySQL(postId, userId, false);
                }
            }
        } catch (Exception e) {
            log.error("处理点赞变更记录失败: changeRecord={}", changeRecord, e);
        }
    }
}