package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trip.model.entity.PostCollect;
import com.trip.model.vo.PostCollectVO;
import com.trip.web.mapper.PostCollectMapper;
import com.trip.web.service.PostCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子收藏服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostCollectServiceImpl implements PostCollectService {
    
    private final PostCollectMapper postCollectMapper;
    
    @Override
    @Transactional
    public PostCollectVO collectPost(Long postId, Long userId) {
        PostCollectVO result = new PostCollectVO();
        result.setPostId(postId);
        
        try {
            // 检查是否已经收藏
            Boolean isCollected = postCollectMapper.isCollectedByUser(postId, userId);
            if (isCollected) {
                result.setMessage("已经收藏过该帖子");
                result.setIsCollected(true);
                result.setCollectCount(postCollectMapper.getCollectCount(postId));
                return result;
            }
            
            // 检查是否存在已删除的收藏记录，如果存在则恢复
            PostCollect existingCollect = postCollectMapper.selectOne(
                new LambdaQueryWrapper<PostCollect>()
                    .eq(PostCollect::getPostId, postId)
                    .eq(PostCollect::getUserId, userId)
            );
            
            if (existingCollect != null) {
                // 恢复已删除的收藏记录
                existingCollect.setIsDeleted((byte) 0);
                postCollectMapper.updateById(existingCollect);
                log.info("恢复用户 {} 对帖子 {} 的收藏", userId, postId);
            } else {
                // 创建新的收藏记录
                PostCollect postCollect = new PostCollect();
                postCollect.setPostId(postId);
                postCollect.setUserId(userId);
                postCollectMapper.insert(postCollect);
                log.info("用户 {} 收藏帖子 {}", userId, postId);
            }
            
            result.setIsCollected(true);
            result.setCollectCount(postCollectMapper.getCollectCount(postId));
            result.setMessage("收藏成功");
            
        } catch (Exception e) {
            log.error("收藏帖子失败: postId={}, userId={}", postId, userId, e);
            result.setMessage("收藏失败");
            result.setIsCollected(false);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public PostCollectVO uncollectPost(Long postId, Long userId) {
        PostCollectVO result = new PostCollectVO();
        result.setPostId(postId);
        
        try {
            // 查找收藏记录
            PostCollect postCollect = postCollectMapper.selectOne(
                new LambdaQueryWrapper<PostCollect>()
                    .eq(PostCollect::getPostId, postId)
                    .eq(PostCollect::getUserId, userId)
                    .eq(PostCollect::getIsDeleted, 0)
            );
            
            if (postCollect == null) {
                result.setMessage("未收藏该帖子");
                result.setIsCollected(false);
                result.setCollectCount(postCollectMapper.getCollectCount(postId));
                return result;
            }
            
            // 软删除收藏记录
            postCollect.setIsDeleted((byte) 1);
            postCollectMapper.updateById(postCollect);
            
            result.setIsCollected(false);
            result.setCollectCount(postCollectMapper.getCollectCount(postId));
            result.setMessage("取消收藏成功");
            
            log.info("用户 {} 取消收藏帖子 {}", userId, postId);
            
        } catch (Exception e) {
            log.error("取消收藏帖子失败: postId={}, userId={}", postId, userId, e);
            result.setMessage("取消收藏失败");
            result.setIsCollected(true);
        }
        
        return result;
    }
    
    @Override
    public Boolean isPostCollectedByUser(Long postId, Long userId) {
        try {
            return postCollectMapper.isCollectedByUser(postId, userId);
        } catch (Exception e) {
            log.error("检查收藏状态失败: postId={}, userId={}", postId, userId, e);
            return false;
        }
    }
    
    @Override
    public Long getPostCollectCount(Long postId) {
        try {
            return postCollectMapper.getCollectCount(postId);
        } catch (Exception e) {
            log.error("获取帖子收藏数失败: postId={}", postId, e);
            return 0L;
        }
    }
}