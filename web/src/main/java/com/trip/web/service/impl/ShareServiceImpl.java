package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Share;
import com.trip.web.service.ShareService;
import com.trip.web.mapper.ShareMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【share(分享信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share>
    implements ShareService{

}




