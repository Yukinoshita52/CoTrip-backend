package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Invitation;
import com.trip.web.service.InvitationService;
import com.trip.web.mapper.InvitationMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【invitation(邀请信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class InvitationServiceImpl extends ServiceImpl<InvitationMapper, Invitation>
    implements InvitationService{

}




