package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.model.dto.AccountBookDTO;
import com.trip.model.dto.AccountBookDetailDTO;
import com.trip.model.dto.MemberPayDTO;
import com.trip.model.dto.RecordDTO;
import com.trip.model.entity.AccountBookRecord;
import com.trip.model.entity.Book;
import com.trip.model.entity.BookUser;
import com.trip.model.entity.TripUser;
import com.trip.model.vo.*;
import com.trip.web.mapper.AccountBookRecordMapper;
import com.trip.web.mapper.BookMapper;
import com.trip.web.mapper.BookUserMapper;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.service.AccountService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Resource
    private TripUserMapper tripUserMapper;

    @Override
    @Transactional
    public AccountBookVO createAccountBook(Long userId, AccountBookDTO dto) {
        //1.创建账本
        Book book = new Book();
        book.setTripId(dto.getTripId());
        book.setName(dto.getName());
        bookMapper.insert(book);
        
        //2.获取行程的所有成员
        List<TripUser> tripMembers = tripUserMapper.selectList(
            new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, dto.getTripId())
                .eq(TripUser::getIsDeleted, 0)
        );
        
        //3.将所有行程成员添加到账本中
        for (TripUser tripMember : tripMembers) {
            BookUser bookUser = new BookUser();
            bookUser.setBookId(book.getId());
            bookUser.setUserId(tripMember.getUserId());
            bookUserMapper.insert(bookUser);
        }

        //4.数据封装与返回
        AccountBookVO res = new AccountBookVO();
        res.setBookId(book.getId());
        res.setTripId(dto.getTripId());
        res.setName(dto.getName());
        return res;
    }

    @Override
    public List<AccountBookDetailVO> getAllAccountBooks(Long userId) {
        System.out.println("AccountServiceImpl: 查询用户 " + userId + " 的账本列表");
        List<AccountBookDetailDTO> accountBooks = bookMapper.getAccountBookByUserId(userId);
        System.out.println("AccountServiceImpl: 从数据库查询到 " + accountBooks.size() + " 条记录");
        
        for (AccountBookDetailDTO dto : accountBooks) {
            System.out.println("AccountServiceImpl: 记录 - bookId=" + dto.getBookId() + 
                ", name=" + dto.getName() + ", tripId=" + dto.getTripId() + 
                ", amount=" + dto.getTotalAmount() + ", type=" + dto.getType());
        }

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
                // 初始化金额为0
                vo.setTotalIncome(BigDecimal.ZERO);
                vo.setTotalExpense(BigDecimal.ZERO);
                System.out.println("AccountServiceImpl: 创建新账本VO - bookId=" + bookId + ", name=" + accountBook.getName());
            }
            vo = map.get(bookId);
            // 1表示收入，2表示支出
            if(accountBook.getType() != null) {
                if(accountBook.getType() == 1){
                    vo.setTotalIncome(accountBook.getTotalAmount());
                    System.out.println("AccountServiceImpl: 设置收入 - bookId=" + bookId + ", income=" + accountBook.getTotalAmount());
                }else if(accountBook.getType() == 2){
                    vo.setTotalExpense(accountBook.getTotalAmount());
                    System.out.println("AccountServiceImpl: 设置支出 - bookId=" + bookId + ", expense=" + accountBook.getTotalAmount());
                }
            }
        }
        
        List<AccountBookDetailVO> result = new LinkedList<>(map.values());
        System.out.println("AccountServiceImpl: 最终返回 " + result.size() + " 个账本");
        for (AccountBookDetailVO vo : result) {
            System.out.println("AccountServiceImpl: 返回账本 - bookId=" + vo.getBookId() + 
                ", name=" + vo.getName() + ", tripId=" + vo.getTripId());
        }
        
        return result;
    }

    @Override
    public void removeRecordById(Long recordId) {
        accountBookRecordMapper.deleteById(recordId);
    }

    @Override
    public RecordVO addRecord(Long userId, RecordDTO record) {
        AccountBookRecord accountBookRecord = new AccountBookRecord();
        accountBookRecord.setUserId(userId);
        accountBookRecord.setBookId(record.getBookId());
        // 如果categoryId为null，设置为默认值（1表示"其他"类别）
        // 确保categoryId不为null，避免数据库约束错误
        Long categoryId = record.getCategoryId();
        if (categoryId == null) {
            // 默认使用ID为1的类别（其他）
            categoryId = 1L;
        }
        accountBookRecord.setCategoryId(categoryId);
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
        List<RecordVO> records = accountBookRecordMapper.pageRecords(bookId,(page-1)*size,size);

        RecordPageVO res = new RecordPageVO();
        res.setPage(page);
        res.setSize(size);
        res.setTotal(records.size());
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
        if(record.getAmount() != null){
            accountBookRecord.setAmount(record.getAmount());
        }
        if(record.getNote() != null){
            accountBookRecord.setRemark(record.getNote());
        }
        if(record.getCategoryId() != null){
            accountBookRecord.setCategoryId(record.getCategoryId());
        }
        if(record.getType() != null){
            accountBookRecord.setType(record.getType());
        }
        if(record.getRecordTime() != null){
            accountBookRecord.setRecordTime(record.getRecordTime());
        }
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
                    // 1. 创建新对象
                    BookStatsVO.CategoryStat newStat = new BookStatsVO.CategoryStat();
                    // 2. 设置属性
                    newStat.setCategoryName(categoryName);
                    // 3. 放入Map
                    categoryStats.put(categoryName, newStat);
                }
                BookStatsVO.CategoryStat categoryStat = categoryStats.get(categoryName);
                //累加该分类下的花费（expense）
                categoryStat.setExpense(categoryStat.getExpense().add(record.getAmount()));

                if(!dailyStats.containsKey(date)){
                    BookStatsVO.DailyStat dailyStat = new BookStatsVO.DailyStat();
                    dailyStat.setDate(date);
                    dailyStats.put(date, dailyStat);
                }
                BookStatsVO.DailyStat dailyStat = dailyStats.get(date);
                dailyStat.setExpense(dailyStat.getExpense().add(record.getAmount()));
            }else{
                //累加总收入
                totalIncome = totalIncome.add(record.getAmount());

                if(!dailyStats.containsKey(date)){
                    BookStatsVO.DailyStat dailyStat = new BookStatsVO.DailyStat();
                    dailyStat.setDate(date);
                    dailyStats.put(date, dailyStat);
                }
                BookStatsVO.DailyStat dailyStat = dailyStats.get(date);
                dailyStat.setIncome(dailyStat.getIncome().add(record.getAmount()));
            }
        }

        BookStatsVO res = new BookStatsVO();
        res.setBookId(bookId);
        res.setTotalExpense(totalExpense);
        res.setTotalIncome(totalIncome);

        // 修正: 使用 new ArrayList() 将 Collection 转换为 List
        res.setCategoryStats(new ArrayList<>(categoryStats.values()));
        res.setDailyStats(new ArrayList<>(dailyStats.values()));
//        res.setCategoryStats((List<BookStatsVO.CategoryStat>) categoryStats.values());
//        res.setDailyStats((List<BookStatsVO.DailyStat>) dailyStats.values());
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
            if(memberPay.getExpense() == null){
                subRes.setShouldPay(BigDecimal.ZERO);
            }else{
                subRes.setShouldPay(memberPay.getExpense().divide(new BigDecimal(n),2, RoundingMode.HALF_UP));
            }
            res.add(subRes);
        }
        return res;
    }
    
    @Override
    @Transactional
    public void addMemberToTripBooks(Long tripId, Long userId) {
        // 1.获取该行程的所有账本
        List<Book> tripBooks = bookMapper.selectList(
            new LambdaQueryWrapper<Book>()
                .eq(Book::getTripId, tripId)
                .eq(Book::getIsDeleted, 0)
        );
        
        // 2.将用户添加到每个账本中
        for (Book book : tripBooks) {
            // 检查用户是否已经在账本中
            BookUser existingBookUser = bookUserMapper.selectOne(
                new LambdaQueryWrapper<BookUser>()
                    .eq(BookUser::getBookId, book.getId())
                    .eq(BookUser::getUserId, userId)
                    .eq(BookUser::getIsDeleted, 0)
            );
            
            // 如果用户不在账本中，则添加
            if (existingBookUser == null) {
                BookUser bookUser = new BookUser();
                bookUser.setBookId(book.getId());
                bookUser.setUserId(userId);
                bookUserMapper.insert(bookUser);
            }
        }
    }
}
