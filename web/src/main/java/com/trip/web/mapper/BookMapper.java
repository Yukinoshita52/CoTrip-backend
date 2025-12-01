package com.trip.web.mapper;

import com.trip.model.dto.AccountBookDTO;
import com.trip.model.dto.AccountBookDetailDTO;
import com.trip.model.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.AccountBookDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 26423
* @description 针对表【book】的数据库操作Mapper
* @createDate 2025-11-29 15:05:15
* @Entity com.trip.model.entity.Book
*/
public interface BookMapper extends BaseMapper<Book> {

    List<AccountBookDetailDTO> getAccountBookByUserId(@Param("userId") Long userId);
}




