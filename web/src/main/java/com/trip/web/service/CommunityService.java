package com.trip.web.service;

import com.trip.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.FeedPageVO;

/**
 * ClassName: CommunityService
 * Package: com.trip.web.service
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 17:34
 * @Version 1.0
 */
public interface CommunityService extends IService<Post> {
    FeedPageVO getFeed(Integer page, Integer size);
}