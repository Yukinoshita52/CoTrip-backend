package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trip.model.entity.Post;
import com.trip.model.entity.Trip;
import com.trip.model.entity.User;
import com.trip.model.vo.AdminStatisticsVO;
import com.trip.web.mapper.PostMapper;
import com.trip.web.mapper.TripMapper;
import com.trip.web.mapper.UserMapper;
import com.trip.web.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理员统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {
    
    private final UserMapper userMapper;
    private final TripMapper tripMapper;
    private final PostMapper postMapper;
    
    @Override
    public AdminStatisticsVO getStatistics() {
        AdminStatisticsVO stats = new AdminStatisticsVO();
        
        // 统计用户总数（未删除的）
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)
        );
        stats.setTotalUsers(userCount != null ? userCount : 0L);
        
        // 统计行程总数（未删除的）
        Long tripCount = tripMapper.selectCount(
            new LambdaQueryWrapper<Trip>()
                .eq(Trip::getIsDeleted, 0)
        );
        stats.setTotalTrips(tripCount != null ? tripCount : 0L);
        
        // 统计社区帖子总数（未删除的）
        Long postCount = postMapper.selectCount(
            new LambdaQueryWrapper<Post>()
                .eq(Post::getIsDeleted, 0)
        );
        stats.setTotalPosts(postCount != null ? postCount : 0L);
        
        return stats;
    }
}

