package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.model.dto.AccountBookDTO;
import com.trip.model.dto.AccountBookDetailDTO;
import com.trip.model.dto.MemberPayDTO;
import com.trip.model.dto.RecordDTO;
import com.trip.model.entity.AccountBookRecord;
import com.trip.model.entity.Book;
import com.trip.model.entity.BookUser;
import com.trip.model.vo.*;
import com.trip.web.mapper.AccountBookRecordMapper;
import com.trip.web.mapper.BookMapper;
import com.trip.web.mapper.BookUserMapper;
import com.trip.web.service.AccountService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * ClassName: AccountServiceImpl
 * Package: com.trip.web.service.impl
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/29 15:39
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Resource
    private BookMapper bookMapper;

    @Resource
    private final AccountBookRecordMapper accountBookRecordMapper;

    @Resource
    private final BookUserMapper bookUserMapper;

    @Override
    public AccountBookVO createAccountBook(Long userId,AccountBookDTO dto) {
        //1.业务实现
        Book book = new Book();
        book.setTripId(dto.getTripId());
        book.setName(dto.getName());
        bookMapper.insert(book);
        BookUser bookUser = new BookUser();
        bookUser.setBookId(book.getId());
        bookUser.setUserId(userId);
        bookUserMapper.insert(bookUser);

        //2.数据封装与返回
        AccountBookVO res = new AccountBookVO();
        res.setBookId(book.getId());
        res.setTripId(dto.getTripId());
        res.setName(dto.getName());
        return res;
    }

    @Override
    public List<AccountBookDetailVO> getAllAccountBooks(Long userId) {
        List<AccountBookDetailDTO> accountBooks = bookMapper.getAccountBookByUserId(userId);

        Map<Long,AccountBookDetailVO> map = new HashMap<>();
        for (AccountBookDetailDTO accountBook : accountBooks) {
            Long bookId = accountBook.getBookId();
            AccountBookDetailVO vo;
            if(!map.containsKey(bookId)){
                map.put(bookId,new AccountBookDetailVO());
                vo = map.get(bookId);
                vo.setBookId(bookId);
                vo.setName(accountBook.getName());
                vo.setTripId(accountBook.getTripId());
            }
            vo = map.get(bookId);
            if(accountBook.getType() == 1L){
                vo.setTotalExpense(accountBook.getTotalAmount());
            }else{
                vo.setTotalIncome(accountBook.getTotalAmount());
            }
        }
        return new LinkedList<>(map.values());
    }

    @Override
    public void removeBookById(Long bookId) {
        bookMapper.deleteById(bookId);
    }

    @Override
    public RecordVO addRecord(Long userId, RecordDTO record) {
        AccountBookRecord accountBookRecord = new AccountBookRecord();
        accountBookRecord.setUserId(userId);
        accountBookRecord.setBookId(record.getBookId());
        accountBookRecord.setCategoryId(record.getCategoryId());
        accountBookRecord.setAmount(record.getAmount());
        accountBookRecord.setType(record.getType());
        accountBookRecord.setRecordTime(record.getRecordTime());
        accountBookRecord.setRemark(record.getNote());
        accountBookRecordMapper.insert(accountBookRecord);

        RecordVO res = new RecordVO();
        res.setRecordId(accountBookRecord.getId());
        res.setBookId(accountBookRecord.getBookId());
        return res;
    }

    @Override
    public RecordPageVO pageRecords(Long bookId, Integer page, Integer size) {
        List<RecordVO> records = accountBookRecordMapper.pageRecords(bookId,page,size);

        RecordPageVO res = new RecordPageVO();
        res.setPage(page);
        res.setSize(size);
        res.setLists(records);
        return res;
    }

    @Override
    public RecordVO getRecord(Long recordId) {
        return accountBookRecordMapper.getRecord(recordId);
    }

    @Override
    public RecordVO updateRecord(Long recordId, RecordDTO record) {
        AccountBookRecord accountBookRecord = accountBookRecordMapper.selectById(recordId);
        accountBookRecord.setAmount(record.getAmount());
        accountBookRecord.setRemark(record.getNote());
        accountBookRecordMapper.updateById(accountBookRecord);
        RecordVO res = new RecordVO();
        res.setRecordId(recordId);
        res.setBookId(accountBookRecord.getBookId());
        return res;
    }

    @Override
    public BookStatsVO getBookStats(Long bookId) {
        List<RecordDTO> records = accountBookRecordMapper.listRecordInfoByBookId(bookId);
        //将records装换为BookStatsVO
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        //1.按分类名称categoryName分组，得到categoryStats
        //2.按日期date分组，得到每组的income、expense
        Map<String,BookStatsVO.CategoryStat> categoryStats = new HashMap<>();
        Map<Date,BookStatsVO.DailyStat> dailyStats = new HashMap<>();
        for (RecordDTO record : records) {
            String categoryName = record.getCategoryName();
            Date date = record.getRecordTime();

            //1为花费、2为收入
            if(record.getType() == 1L){
                //累加总花费
                totalExpense = totalExpense.add(record.getAmount());
                //若无该类型，则新增此类型的key
                if(!categoryStats.containsKey(categoryName)){
                    Objects.requireNonNull(categoryStats.put(categoryName, new BookStatsVO.CategoryStat())).setCategoryName(categoryName);
                }
                BookStatsVO.CategoryStat categoryStat = categoryStats.get(categoryName);
                //累加该分类下的花费（expense）
                categoryStat.setExpense(categoryStat.getExpense().add(record.getAmount()));

                if(!dailyStats.containsKey(date)){
                    Objects.requireNonNull(dailyStats.put(date, new BookStatsVO.DailyStat())).setDate(date);
                }
                BookStatsVO.DailyStat dailyStat = dailyStats.get(date);
                dailyStat.setExpense(dailyStat.getExpense().add(record.getAmount()));
            }else{
                //累加总收入
                totalIncome = totalIncome.add(record.getAmount());

                if(!dailyStats.containsKey(date)){
                    Objects.requireNonNull(dailyStats.put(date, new BookStatsVO.DailyStat())).setDate(date);
                }
                BookStatsVO.DailyStat dailyStat = dailyStats.get(date);
                dailyStat.setIncome(dailyStat.getIncome().add(record.getAmount()));
            }
        }

        BookStatsVO res = new BookStatsVO();
        res.setBookId(bookId);
        res.setTotalExpense(totalExpense);
        res.setTotalIncome(totalIncome);
        res.setCategoryStats((List<BookStatsVO.CategoryStat>) categoryStats.values());
        res.setDailyStats((List<BookStatsVO.DailyStat>) dailyStats.values());
        return res;
    }

    @Override
    public List<PayMemberVO> splitAmount(Long bookId, Long userId) {
        List<MemberPayDTO> memberPays = accountBookRecordMapper.getMemberPays(bookId);
        int n = memberPays.size();//总人数
        List<PayMemberVO> res = new ArrayList<>(n-1);
        for(MemberPayDTO memberPay : memberPays){
            if(memberPay.getUserId().equals(userId)) continue;//自己不用付自己钱

            PayMemberVO subRes = new PayMemberVO();
            subRes.setUserId(memberPay.getUserId());
            subRes.setNickname(memberPay.getNickname());
            subRes.setShouldPay(memberPay.getExpense().divide(new BigDecimal(n),2, RoundingMode.HALF_UP));
            res.add(subRes);
        }
        return res;
    }
}
