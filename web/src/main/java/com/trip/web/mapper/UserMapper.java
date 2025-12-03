package com.trip.web.mapper;

import com.trip.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.AuthorVO;
import com.trip.model.vo.UserPostVO;
import com.trip.model.vo.UserPostsStatsVO;
import com.trip.model.vo.UserProfileVO;

import java.util.List;

/**
* @author 26423
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2025-10-05 23:38:17
* @Entity com.trip.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    AuthorVO getAuthorVoByUserId(Long userId);

    UserPostsStatsVO getUserPostStatsByUserId(Long userId);

    List<UserPostVO> getUserPostsByUserId(Long userId);

    List<AuthorVO> getAuthorVoByKeyword(String keyword);
}




