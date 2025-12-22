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
    @Transactional
    public void deleteAccountBook(Long bookId, Long userId) {
        // 验证用户权限（检查用户是否有权限删除此账本）
        BookUser bookUser = bookUserMapper.selectOne(
            new LambdaQueryWrapper<BookUser>()
                .eq(BookUser::getBookId, bookId)
                .eq(BookUser::getUserId, userId)
                .eq(BookUser::getIsDeleted, 0)
        );
        
        if (bookUser == null) {
            throw new RuntimeException("无权删除此账本");
        }
        
        // 1. 删除账本记录
        List<AccountBookRecord> records = accountBookRecordMapper.selectList(
            new LambdaQueryWrapper<AccountBookRecord>()
                .eq(AccountBookRecord::getBookId, bookId)
                .eq(AccountBookRecord::getIsDeleted, 0)
        );
        
        for (AccountBookRecord record : records) {
            record.setIsDeleted((byte) 1);
            accountBookRecordMapper.updateById(record);
        }
        
        // 2. 删除账本用户关系
        List<BookUser> bookUsers = bookUserMapper.selectList(
            new LambdaQueryWrapper<BookUser>()
                .eq(BookUser::getBookId, bookId)
                .eq(BookUser::getIsDeleted, 0)
        );
        
        for (BookUser bu : bookUsers) {
            bu.setIsDeleted((byte) 1);
            bookUserMapper.updateById(bu);
        }
        
        // 3. 删除账本
        Book book = bookMapper.selectById(bookId);
        if (book != null) {
            book.setIsDeleted((byte) 1);
            bookMapper.updateById(book);
        }
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
        System.out.println("AccountServiceImpl.pageRecords: bookId=" + bookId + ", page=" + page + ", size=" + size);
        List<RecordVO> records = accountBookRecordMapper.pageRecords(bookId,(page-1)*size,size);
        System.out.println("AccountServiceImpl.pageRecords: 查询到 " + records.size() + " 条记录");
        
        for (int i = 0; i < records.size(); i++) {
            RecordVO record = records.get(i);
            System.out.println("记录 " + (i + 1) + ": recordId=" + record.getRecordId() + 
                ", note='" + record.getNote() + "', amount=" + record.getAmount() + 
                ", categoryName=" + record.getCategoryName());
        }
        
        // 获取总记录数
        LambdaQueryWrapper<AccountBookRecord> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(AccountBookRecord::getBookId, bookId)
                   .eq(AccountBookRecord::getIsDeleted, 0);
        long totalCount = accountBookRecordMapper.selectCount(countWrapper);
        System.out.println("AccountServiceImpl.pageRecords: 总记录数=" + totalCount);

        RecordPageVO res = new RecordPageVO();
        res.setPage(page);
        res.setSize(size);
        res.setTotal((int) totalCount); // 设置正确的总记录数
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
        // 始终更新remark字段，允许设置为空字符串
        accountBookRecord.setRemark(record.getNote());
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
        // 1. 获取账本中所有成员
        List<MemberPayDTO> memberPays = accountBookRecordMapper.getMemberPays(bookId);
        int totalMembers = memberPays.size(); // 总人数
        
        if (totalMembers == 0) {
            return new ArrayList<>();
        }
        
        // 2. 计算总支出（只计算type=1的支出记录）
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (MemberPayDTO memberPay : memberPays) {
            if (memberPay.getExpense() != null) {
                totalExpense = totalExpense.add(memberPay.getExpense());
            }
        }
        
        // 3. 计算每人应该承担的金额
        BigDecimal perPersonAmount = totalExpense.divide(new BigDecimal(totalMembers), 2, RoundingMode.HALF_UP);
        
        // 4. 计算当前用户实际支付的金额
        BigDecimal currentUserPaid = BigDecimal.ZERO;
        for (MemberPayDTO memberPay : memberPays) {
            if (memberPay.getUserId().equals(userId) && memberPay.getExpense() != null) {
                currentUserPaid = memberPay.getExpense();
                break;
            }
        }
        
        // 5. 计算分摊结果
        List<PayMemberVO> result = new ArrayList<>();
        
        for (MemberPayDTO memberPay : memberPays) {
            if (memberPay.getUserId().equals(userId)) {
                continue; // 跳过自己
            }
            
            PayMemberVO payMemberVO = new PayMemberVO();
            payMemberVO.setUserId(memberPay.getUserId());
            payMemberVO.setNickname(memberPay.getNickname());
            
            // 计算该成员实际支付的金额
            BigDecimal memberPaid = memberPay.getExpense() != null ? memberPay.getExpense() : BigDecimal.ZERO;
            
            // 最简单直接的逻辑：
            // 每个人都应该承担 perPersonAmount
            // 如果该成员支付不足，当前用户需要补贴给该成员
            // 如果该成员支付过多，该成员需要从当前用户那里收回
            
            BigDecimal memberShouldPay = perPersonAmount;
            BigDecimal memberActuallyPaid = memberPaid;
            BigDecimal memberDeficit = memberShouldPay.subtract(memberActuallyPaid);
            
            // 该成员应该付给当前用户的金额就是该成员的不足部分
            // 如果为正数，表示该成员欠当前用户钱
            // 如果为负数，表示当前用户欠该成员钱
            payMemberVO.setShouldPay(memberDeficit);
            
            result.add(payMemberVO);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void addMemberToTripBooks(Long tripId, Long userId) {
        System.out.println("addMemberToTripBooks: 为用户 " + userId + " 添加行程 " + tripId + " 的账本权限");
        
        // 1.获取该行程的所有账本
        List<Book> tripBooks = bookMapper.selectList(
            new LambdaQueryWrapper<Book>()
                .eq(Book::getTripId, tripId)
                .eq(Book::getIsDeleted, 0)
        );
        
        System.out.println("addMemberToTripBooks: 行程 " + tripId + " 有 " + tripBooks.size() + " 个账本");
        
        // 2.将用户添加到每个账本中
        for (Book book : tripBooks) {
            System.out.println("addMemberToTripBooks: 处理账本 " + book.getId() + " (" + book.getName() + ")");
            
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
                System.out.println("addMemberToTripBooks: 已将用户 " + userId + " 添加到账本 " + book.getId());
            } else {
                System.out.println("addMemberToTripBooks: 用户 " + userId + " 已在账本 " + book.getId() + " 中");
            }
        }
        
        System.out.println("addMemberToTripBooks: 完成为用户 " + userId + " 添加行程 " + tripId + " 的账本权限");
    }
    
    @Override
    @Transactional
    public void removeMemberFromTripBooks(Long tripId, Long userId) {
        System.out.println("removeMemberFromTripBooks: 为用户 " + userId + " 移除行程 " + tripId + " 的账本权限");
        
        // 1.获取该行程的所有账本
        List<Book> tripBooks = bookMapper.selectList(
            new LambdaQueryWrapper<Book>()
                .eq(Book::getTripId, tripId)
                .eq(Book::getIsDeleted, 0)
        );
        
        System.out.println("removeMemberFromTripBooks: 行程 " + tripId + " 有 " + tripBooks.size() + " 个账本");
        
        // 2.将用户从每个账本中移除
        for (Book book : tripBooks) {
            System.out.println("removeMemberFromTripBooks: 处理账本 " + book.getId() + " (" + book.getName() + ")");
            
            // 查找用户在账本中的记录
            BookUser bookUser = bookUserMapper.selectOne(
                new LambdaQueryWrapper<BookUser>()
                    .eq(BookUser::getBookId, book.getId())
                    .eq(BookUser::getUserId, userId)
                    .eq(BookUser::getIsDeleted, 0)
            );
            
            // 如果用户在账本中，则软删除
            if (bookUser != null) {
                bookUser.setIsDeleted((byte) 1);
                bookUserMapper.updateById(bookUser);
                System.out.println("removeMemberFromTripBooks: 已将用户 " + userId + " 从账本 " + book.getId() + " 中移除");
            } else {
                System.out.println("removeMemberFromTripBooks: 用户 " + userId + " 不在账本 " + book.getId() + " 中");
            }
        }
        
        System.out.println("removeMemberFromTripBooks: 完成为用户 " + userId + " 移除行程 " + tripId + " 的账本权限");
    }
    
    @Override
    public BigDecimal getMyExpenseAmount(Long bookId, Long userId) {
        // 查询用户在指定账本中的支出总金额（只统计type=1的支出记录）
        List<AccountBookRecord> records = accountBookRecordMapper.selectList(
            new LambdaQueryWrapper<AccountBookRecord>()
                .eq(AccountBookRecord::getBookId, bookId)
                .eq(AccountBookRecord::getUserId, userId)
                .eq(AccountBookRecord::getType, 1) // 只统计支出
                .eq(AccountBookRecord::getIsDeleted, 0)
        );
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (AccountBookRecord record : records) {
            if (record.getAmount() != null) {
                totalAmount = totalAmount.add(record.getAmount());
            }
        }
        
        return totalAmount;
    }
    
    @Override
    public Integer getMyExpenseCount(Long bookId, Long userId) {
        // 查询用户在指定账本中的账单数量（只统计type=1的支出记录）
        Long count = accountBookRecordMapper.selectCount(
            new LambdaQueryWrapper<AccountBookRecord>()
                .eq(AccountBookRecord::getBookId, bookId)
                .eq(AccountBookRecord::getUserId, userId)
                .eq(AccountBookRecord::getType, 1) // 只统计支出
                .eq(AccountBookRecord::getIsDeleted, 0)
        );
        
        return count.intValue();
    }
    
    @Override
    public Integer getTotalMembersCount(Long bookId) {
        // 查询账本中的总成员数
        Long count = bookUserMapper.selectCount(
            new LambdaQueryWrapper<BookUser>()
                .eq(BookUser::getBookId, bookId)
                .eq(BookUser::getIsDeleted, 0)
        );
        
        return count.intValue();
    }
}
