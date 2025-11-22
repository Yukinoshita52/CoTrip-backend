package com.trip.web.service;

import com.trip.model.dto.TripDTO;
import com.trip.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.FeedPageVO;
import com.trip.model.vo.PostCreatedVO;
import com.trip.model.vo.PostDetailVO;

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

    PostDetailVO getPostDetail(Long postId);

    /**
     * 根据tripId创建帖子（也就是用户分享了自己的行程到社区）
     * @param dto
     * @return
     */
    PostCreatedVO createPost(TripDTO dto);

}