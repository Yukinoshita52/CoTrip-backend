package com.trip.web.mapper;

import com.trip.model.dto.MemberPayDTO;
import com.trip.model.dto.RecordDTO;
import com.trip.model.entity.AccountBookRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.RecordVO;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.optional.qual.Present;
import org.springframework.security.core.parameters.P;

import java.util.List;

/**
* @author 26423
* @description 针对表【account_book_record】的数据库操作Mapper
* @createDate 2025-11-29 15:05:15
* @Entity com.trip.model.entity.AccountBookRecord
*/
public interface AccountBookRecordMapper extends BaseMapper<AccountBookRecord> {

    List<RecordVO> pageRecords(@Param("bookId") Long bookId, @Param("page") Integer page,@Param("size") Integer size);

    RecordVO getRecord(@Param("recordId") Long recordId);

    List<RecordDTO> listRecordInfoByBookId(Long bookId);

    List<MemberPayDTO> getMemberPays(@Param("bookId") Long bookId);
}




