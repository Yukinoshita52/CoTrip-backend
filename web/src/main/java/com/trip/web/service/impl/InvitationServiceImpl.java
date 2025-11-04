package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.Invitation;
import com.trip.model.entity.User;
import com.trip.model.vo.InvitationVO;
import com.trip.web.mapper.InvitationMapper;
import com.trip.web.service.GraphInfoService;
import com.trip.web.service.InvitationService;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 26423
* @description 针对表【invitation(邀请信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl extends ServiceImpl<InvitationMapper, Invitation>
    implements InvitationService{

    private final UserService userService;
    private final GraphInfoService graphInfoService;

    @Override
    public List<InvitationVO> getSentInvitations(Long inviterId) {
        List<Invitation> invitations = this.list(new LambdaQueryWrapper<Invitation>()
                .eq(Invitation::getInviterId, inviterId)
                .orderByDesc(Invitation::getCreateTime));

        return convertToVOList(invitations);
    }

    @Override
    public List<InvitationVO> getReceivedInvitations(String phone) {
        // 查询状态为待接受(0)的邀请
        List<Invitation> invitations = this.list(new LambdaQueryWrapper<Invitation>()
                .eq(Invitation::getInvitee, phone)
                .eq(Invitation::getStatus, 0)
                .orderByDesc(Invitation::getCreateTime));

        return convertToVOList(invitations);
    }

    @Override
    public void createInvitation(Long inviterId, String invitee) {
        // 验证邀请人是否存在
        User inviter = userService.getById(inviterId);
        if (inviter == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 验证被邀请人是否存在
        User inviteeUser = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, invitee));
        if (inviteeUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 不能邀请自己
        if (inviter.getPhone().equals(invitee)) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "不能邀请自己");
        }

        // 检查是否已有待处理的邀请
        long count = this.count(new LambdaQueryWrapper<Invitation>()
                .eq(Invitation::getInviterId, inviterId)
                .eq(Invitation::getInvitee, invitee)
                .eq(Invitation::getStatus, 0));
        if (count > 0) {
            throw new LeaseException(ResultCodeEnum.REPEAT_SUBMIT.getCode(), "已存在待处理的邀请");
        }

        // 创建邀请
        Invitation invitation = new Invitation();
        invitation.setInviterId(inviterId);
        invitation.setInvitee(invitee);
        invitation.setStatus(0); // 待接受
        this.save(invitation);

        // TODO: 发送通知给被邀请人
    }

    @Override
    public void processInvitation(Long invitationId, Integer action) {
        Invitation invitation = this.getById(invitationId);
        if (invitation == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "邀请不存在");
        }

        // 只能处理待接受的邀请
        if (invitation.getStatus() != 0) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "该邀请已处理");
        }

        // 验证处理动作
        if (action != 1 && action != 2) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "处理动作无效");
        }

        // 更新邀请状态
        invitation.setStatus(action); // 1-已接受，2-已拒绝
        this.updateById(invitation);

        // TODO: 如果接受邀请，可以执行相关业务逻辑（如添加好友关系等）
    }

    @Override
    public void cancelInvitation(Long invitationId, Long inviterId) {
        Invitation invitation = this.getById(invitationId);
        if (invitation == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "邀请不存在");
        }

        // 验证权限：只能撤销自己发出的邀请
        if (!invitation.getInviterId().equals(inviterId)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限撤销该邀请");
        }

        // 只能撤销待处理的邀请
        if (invitation.getStatus() != 0) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "只能撤销待处理的邀请");
        }

        // 逻辑删除
        this.removeById(invitationId);
    }

    /**
     * 将 Invitation 列表转换为 InvitationVO 列表
     */
    private List<InvitationVO> convertToVOList(List<Invitation> invitations) {
        return invitations.stream().map(invitation -> {
            InvitationVO vo = new InvitationVO();
            BeanUtils.copyProperties(invitation, vo);

            // 查询邀请人信息
            if (invitation.getInviterId() != null) {
                User inviter = userService.getById(invitation.getInviterId());
                if (inviter != null) {
                    vo.setInviterNickname(inviter.getNickname());
                    vo.setInviterAvatarUrl(graphInfoService.getImageUrlById(inviter.getAvatarId()));
                    vo.setInviterPhone(inviter.getPhone());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}




