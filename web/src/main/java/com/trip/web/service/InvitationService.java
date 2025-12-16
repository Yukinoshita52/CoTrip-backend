package com.trip.web.service;

import com.trip.model.entity.Invitation;
import com.trip.model.vo.InvitationVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 26423
* @description 针对表【invitation(邀请信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface InvitationService extends IService<Invitation> {

    /**
     * 查看我发出的邀请
     * @param inviterId 邀请人ID
     * @return 邀请列表
     */
    List<InvitationVO> getSentInvitations(Long inviterId);

    /**
     * 查看我收到的邀请（待处理邀请）
     * @param phone 当前用户手机号
     * @return 邀请列表
     */
    List<InvitationVO> getReceivedInvitations(String phone);

    /**
     * 发出邀请
     * @param tripId 行程ID
     * @param inviterId 邀请人ID
     * @param invitee 被邀请人手机号
     * @return 创建的邀请信息
     */
    InvitationVO createInvitation(Long tripId, Long inviterId, String invitee);

    /**
     * 处理邀请（同意/拒绝）
     * @param invitationId 邀请ID
     * @param action 处理动作：1-同意，2-拒绝
     */
    void processInvitation(Long invitationId, Integer action);

    /**
     * 撤销邀请
     * @param invitationId 邀请ID
     * @param inviterId 邀请人ID（用于权限验证）
     */
    void cancelInvitation(Long invitationId, Long inviterId);

    /**
     * 删除收到的邀请
     * @param invitationId 邀请ID
     * @param inviteePhone 被邀请人手机号（用于权限验证）
     */
    void deleteReceivedInvitation(Long invitationId, String inviteePhone);
}
