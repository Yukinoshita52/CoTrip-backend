package com.trip.web.service;

import com.trip.model.dto.AccountBookDTO;
import com.trip.model.dto.RecordDTO;
import com.trip.model.vo.*;

import java.util.List;

/**
 * ClassName: AccountService
 * Package: com.trip.web.service
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/29 15:38
 * @Version 1.0
 */
public interface AccountService {

    AccountBookVO createAccountBook(Long userId,AccountBookDTO dto);

    List<AccountBookDetailVO> getAllAccountBooks(Long userId);

    void removeBookById(Long bookId);

    /**
     * 根据用户id和所给的RecordDTO信息，在account_book_category中新增一条记录
     * @param userId
     * @param record
     * @return 返回bookId、recordId
     */
    RecordVO addRecord(Long userId, RecordDTO record);

    RecordPageVO pageRecords(Long bookId, Integer page, Integer size);

    RecordVO getRecord(Long recordId);

    RecordVO updateRecord(Long recordId, RecordDTO record);

    BookStatsVO getBookStats(Long bookId);

    List<PayMemberVO> splitAmount(Long bookId, Long userId);
}
