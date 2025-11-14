package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.result.Result;
import com.trip.model.entity.Share;
import com.trip.web.service.ShareService;
import com.trip.web.mapper.ShareMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author 26423
* @description 针对表【share(分享信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share>
    implements ShareService{

    @Override
    public void insertShare(@RequestParam Long tripId) {
        //直接插入到分享表中
        Share share = new Share();
        share.setTripId(tripId);
        this.save(share);
    }
}




