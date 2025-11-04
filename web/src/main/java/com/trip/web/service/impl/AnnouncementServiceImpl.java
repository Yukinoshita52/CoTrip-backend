package com.trip.web.service.impl;

import com.trip.web.mapper.AnnouncementMapper;
import com.trip.model.entity.Announcement;
import com.trip.model.vo.AnnouncementVO;
import com.trip.web.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<AnnouncementVO> getAnnouncements() {
        log.info("获取所有公告列表");
        List<Announcement> announcements = announcementMapper.selectAll();
        return convertToVOList(announcements);
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        log.info("根据ID获取公告详情: {}", id);
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在，ID: " + id);
        }
        return convertToVO(announcement);
    }

    @Override
    public List<AnnouncementVO> getLatestAnnouncements(int count) {
        log.info("获取最新{}条公告", count);
        List<Announcement> announcements = announcementMapper.selectLatest(count);
        return convertToVOList(announcements);
    }

    @Override
    public AnnouncementVO createAnnouncement(AnnouncementVO announcementVO) {
        log.info("创建新公告: {}", announcementVO.getTitle());
        
        // 设置发布时间为当前时间
        if (announcementVO.getPublishTime() == null) {
            announcementVO.setPublishTime(new java.util.Date());
        }
        
        Announcement announcement = convertToDO(announcementVO);
        int result = announcementMapper.insert(announcement);
        
        if (result > 0) {
            return getAnnouncementById(announcement.getId());
        } else {
            throw new RuntimeException("创建公告失败");
        }
    }

    @Override
    public AnnouncementVO updateAnnouncement(AnnouncementVO announcementVO) {
        log.info("更新公告: {}", announcementVO.getId());
        
        // 检查公告是否存在
        Announcement existingAnnouncement = announcementMapper.selectById(announcementVO.getId());
        if (existingAnnouncement == null) {
            throw new RuntimeException("公告不存在，ID: " + announcementVO.getId());
        }
        
        Announcement announcement = convertToDO(announcementVO);
        int result = announcementMapper.update(announcement);
        
        if (result > 0) {
            return getAnnouncementById(announcementVO.getId());
        } else {
            throw new RuntimeException("更新公告失败");
        }
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        log.info("删除公告: {}", id);
        
        // 检查公告是否存在
        Announcement existingAnnouncement = announcementMapper.selectById(id);
        if (existingAnnouncement == null) {
            throw new RuntimeException("公告不存在，ID: " + id);
        }
        
        int result = announcementMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public List<AnnouncementVO> searchAnnouncements(String keyword) {
        log.info("搜索公告，关键词: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAnnouncements();
        }
        
        List<Announcement> announcements = announcementMapper.searchByKeyword(keyword.trim());
        return convertToVOList(announcements);
    }

    /**
     * DO列表转VO列表
     */
    private List<AnnouncementVO> convertToVOList(List<Announcement> doList) {
        return doList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * DO 转 VO
     */
    private AnnouncementVO convertToVO(Announcement announcementDO) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcementDO, vo);
        return vo;
    }

    /**
     * VO 转 DO
     */
    private Announcement convertToDO(AnnouncementVO announcementVO) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(announcementVO, announcement);
        return announcement;
    }
}