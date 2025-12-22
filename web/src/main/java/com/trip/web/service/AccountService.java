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

    void removeRecordById(Long recordId);

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
    
    /**
     * 当新成员加入行程时，将其添加到该行程的所有账本中
     * @param tripId 行程ID
     * @param userId 新成员用户ID
     */
    void addMemberToTripBooks(Long tripId, Long userId);
    
    /**
     * 当成员退出行程时，将其从该行程的所有账本中移除
     * @param tripId 行程ID
     * @param userId 退出成员用户ID
     */
    void removeMemberFromTripBooks(Long tripId, Long userId);
    
    /**
     * 删除账本及其相关数据
     * @param bookId 账本ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteAccountBook(Long bookId, Long userId);
}
