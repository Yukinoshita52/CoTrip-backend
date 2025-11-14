package com.trip.web.service;

import com.trip.common.result.Result;
import com.trip.model.entity.Share;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 26423
* @description 针对表【share(分享信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface ShareService extends IService<Share> {
    /**
     * 分享行程，根据tripId往share表中插入一条数据。
     * @param tripId
     * @return
     */
    void insertShare(Long tripId);

}
