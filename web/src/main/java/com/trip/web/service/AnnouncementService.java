package com.trip.web.service;

import com.trip.model.vo.AnnouncementVO;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {

    /**
     * 获取公告列表（按发布时间倒序）
     * @return 公告列表
     */
    List<AnnouncementVO> getAnnouncements();

    /**
     * 根据ID获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    AnnouncementVO getAnnouncementById(Long id);

    /**
     * 获取最新的公告（用于首页展示）
     * @param count 获取数量
     * @return 最新公告列表
     */
    List<AnnouncementVO> getLatestAnnouncements(int count);

    /**
     * 创建新公告
     * @param announcementVO 公告信息
     * @return 创建成功的公告信息
     */
    AnnouncementVO createAnnouncement(AnnouncementVO announcementVO);

    /**
     * 更新公告
     * @param announcementVO 公告信息
     * @return 更新后的公告信息
     */
    AnnouncementVO updateAnnouncement(AnnouncementVO announcementVO);

    /**
     * 删除公告
     * @param id 公告ID
     * @return 是否删除成功
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 搜索公告
     * @param keyword 关键词
     * @return 搜索结果列表
     */
    List<AnnouncementVO> searchAnnouncements(String keyword);
}
