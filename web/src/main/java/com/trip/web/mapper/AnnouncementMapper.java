package com.trip.web.mapper;

import com.trip.model.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 公告数据访问接口
 */
@Mapper
public interface AnnouncementMapper {

    /**
     * 查询所有公告（按发布时间倒序）
     */
    @Select("SELECT * FROM announcement ORDER BY publish_time DESC")
    List<Announcement> selectAll();

    /**
     * 根据ID查询公告
     */
    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement selectById(@Param("id") Long id);

    /**
     * 查询最新公告
     */
    @Select("SELECT * FROM announcement ORDER BY publish_time DESC LIMIT #{count}")
    List<Announcement> selectLatest(@Param("count") int count);

    /**
     * 插入公告
     */
    @Insert("INSERT INTO announcement (title, content, publish_time, author_id) " +
            "VALUES (#{title}, #{content}, #{publishTime}, #{authorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    /**
     * 更新公告
     */
    @Update("UPDATE announcement SET title = #{title}, content = #{content}, " +
            "publish_time = #{publishTime}, author_id = #{authorId} WHERE id = #{id}")
    int update(Announcement announcement);

    /**
     * 删除公告
     */
    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 搜索公告（按标题和内容）
     */
    @Select("SELECT * FROM announcement WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') ORDER BY publish_time DESC")
    List<Announcement> searchByKeyword(@Param("keyword") String keyword);
}