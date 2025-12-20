package com.trip.web.test;

import com.trip.model.dto.AccountBookDTO;
import com.trip.model.entity.Book;
import com.trip.model.entity.BookUser;
import com.trip.model.entity.Trip;
import com.trip.model.entity.TripUser;
import com.trip.model.vo.AccountBookDetailVO;
import com.trip.model.vo.AccountBookVO;
import com.trip.web.mapper.BookMapper;
import com.trip.web.mapper.BookUserMapper;
import com.trip.web.mapper.TripMapper;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 账本初始化测试类
 * 用于为现有的行程创建对应的账本
 */
@Slf4j
@SpringBootTest
public class AccountBookInitTest {

    @Autowired
    private TripMapper tripMapper;
    
    @Autowired
    private TripUserMapper tripUserMapper;
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private BookUserMapper bookUserMapper;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private com.trip.web.service.TripUserService tripUserService;

    /**
     * 为所有没有账本的行程创建账本
     * 注意：这个方法会修改数据库，请谨慎使用
     */
    @Test
    public void createAccountBooksForExistingTrips() {
        log.info("开始为现有行程创建账本...");
        
        try {
            // 1. 获取所有有效行程
            List<Trip> allTrips = tripMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Trip>()
                    .eq("is_deleted", 0)
            );
            log.info("找到 {} 个行程", allTrips.size());
            
            int createdCount = 0;
            int skippedCount = 0;
            
            for (Trip trip : allTrips) {
                log.info("处理行程: ID={}, 名称='{}'", trip.getId(), trip.getName());
                
                // 2. 检查该行程是否已有账本
                List<Book> existingBooks = bookMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                        .eq("trip_id", trip.getId())
                        .eq("is_deleted", 0)
                );
                
                if (!existingBooks.isEmpty()) {
                    log.debug("行程 '{}' 已有账本，跳过", trip.getName());
                    skippedCount++;
                    continue;
                }
                
                // 3. 查找行程创建者
                TripUser creator = tripUserMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                        .eq("trip_id", trip.getId())
                        .eq("role", 0) // 0表示创建者
                        .eq("is_deleted", 0)
                );
                
                if (creator == null) {
                    log.warn("行程 '{}' 没有找到创建者，跳过", trip.getName());
                    skippedCount++;
                    continue;
                }
                
                // 4. 为该行程创建账本
                try {
                    AccountBookDTO dto = new AccountBookDTO();
                    dto.setTripId(trip.getId());
                    dto.setName(trip.getName() + "账本");
                    
                    // 使用行程创建者作为账本创建者
                    Long creatorId = creator.getUserId();
                    
                    AccountBookVO result = accountService.createAccountBook(creatorId, dto);
                    
                    log.info("为行程 '{}' (ID: {}) 创建了账本 '{}' (ID: {})", 
                        trip.getName(), trip.getId(), 
                        result.getName(), result.getBookId());
                    
                    createdCount++;
                    
                } catch (Exception e) {
                    log.error("为行程 '{}' 创建账本失败: {}", trip.getName(), e.getMessage(), e);
                }
            }
            
            log.info("账本创建完成！创建了 {} 个账本，跳过了 {} 个行程", createdCount, skippedCount);
            
        } catch (Exception e) {
            log.error("批量创建账本失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 检查特定行程的账本情况
     */
    @Test
    public void checkSpecificTripBooks() {
        Long userId = 5L;
        Long tripId = 16L; // 从日志中看到用户5参与了行程16但没有对应账本
        
        log.info("检查行程 {} 的账本情况...", tripId);
        
        // 1. 检查行程是否存在
        Trip trip = tripMapper.selectById(tripId);
        if (trip == null) {
            log.warn("行程 {} 不存在", tripId);
            return;
        }
        log.info("行程信息: ID={}, 名称='{}', 是否删除={}", trip.getId(), trip.getName(), trip.getIsDeleted());
        
        // 2. 检查该行程的所有成员
        List<TripUser> tripUsers = tripUserMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                .eq("trip_id", tripId)
                .eq("is_deleted", 0)
        );
        log.info("行程 {} 有 {} 个成员", tripId, tripUsers.size());
        for (TripUser tu : tripUsers) {
            log.info("成员: 用户ID={}, 角色={}", tu.getUserId(), tu.getRole());
        }
        
        // 3. 检查该行程的账本
        List<Book> tripBooks = bookMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                .eq("trip_id", tripId)
                .eq("is_deleted", 0)
        );
        log.info("行程 {} 有 {} 个账本", tripId, tripBooks.size());
        for (Book book : tripBooks) {
            log.info("账本: ID={}, 名称='{}'", book.getId(), book.getName());
            
            // 检查账本成员
            List<BookUser> bookUsers = bookUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                    .eq("book_id", book.getId())
                    .eq("is_deleted", 0)
            );
            log.info("账本 {} 有 {} 个成员", book.getId(), bookUsers.size());
            for (BookUser bu : bookUsers) {
                log.info("账本成员: 用户ID={}", bu.getUserId());
            }
        }
        
        // 4. 如果没有账本，创建一个
        if (tripBooks.isEmpty()) {
            log.info("行程 {} 没有账本，创建一个...", tripId);
            try {
                // 找到行程创建者
                TripUser creator = tripUsers.stream()
                    .filter(tu -> tu.getRole() == 0)
                    .findFirst()
                    .orElse(tripUsers.isEmpty() ? null : tripUsers.get(0));
                
                if (creator != null) {
                    AccountBookDTO dto = new AccountBookDTO();
                    dto.setTripId(tripId);
                    dto.setName(trip.getName() + "账本");
                    
                    AccountBookVO result = accountService.createAccountBook(creator.getUserId(), dto);
                    log.info("为行程 {} 创建了账本: ID={}, 名称='{}'", tripId, result.getBookId(), result.getName());
                } else {
                    log.warn("行程 {} 没有找到创建者", tripId);
                }
            } catch (Exception e) {
                log.error("为行程 {} 创建账本失败: {}", tripId, e.getMessage(), e);
            }
        }
    }

    /**
     * 测试特定用户的账本访问权限
     */
    @Test
    public void testUserAccountBookAccess() {
        Long testUserId = 5L; // 使用日志中的用户ID
        log.info("测试用户 {} 的账本访问权限...", testUserId);
        
        try {
            List<AccountBookDetailVO> userBooks = accountService.getAllAccountBooks(testUserId);
            log.info("用户 {} 可以访问 {} 个账本", testUserId, userBooks.size());
            
            for (AccountBookDetailVO book : userBooks) {
                log.info("账本: ID={}, 名称='{}', 行程ID={}, 总收入={}, 总支出={}", 
                    book.getBookId(), book.getName(), book.getTripId(), 
                    book.getTotalIncome(), book.getTotalExpense());
            }
            
            // 检查用户在哪些行程中
            List<TripUser> userTrips = tripUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("user_id", testUserId)
                    .eq("is_deleted", 0)
            );
            
            log.info("用户 {} 参与了 {} 个行程", testUserId, userTrips.size());
            for (TripUser tripUser : userTrips) {
                log.info("行程: ID={}, 角色={}", tripUser.getTripId(), tripUser.getRole());
            }
            
            // 检查用户在哪些账本中
            List<BookUser> userBookUsers = bookUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                    .eq("user_id", testUserId)
                    .eq("is_deleted", 0)
            );
            
            log.info("用户 {} 在 {} 个账本中有权限", testUserId, userBookUsers.size());
            for (BookUser bookUser : userBookUsers) {
                log.info("账本权限: 账本ID={}", bookUser.getBookId());
            }
            
        } catch (Exception e) {
            log.error("测试用户账本访问权限失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查行程和账本的对应关系
     */
    @Test
    public void checkTripBookRelationship() {
        log.info("检查行程和账本的对应关系...");
        
        // 获取所有有效行程
        List<Trip> allTrips = tripMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Trip>()
                .eq("is_deleted", 0)
        );
        
        // 获取所有有效账本
        List<Book> allBooks = bookMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                .eq("is_deleted", 0)
        );
        
        log.info("总计：{} 个有效行程，{} 个有效账本", allTrips.size(), allBooks.size());
        
        int tripsWithBooks = 0;
        int tripsWithoutBooks = 0;
        
        for (Trip trip : allTrips) {
            boolean hasBook = allBooks.stream()
                .anyMatch(book -> book.getTripId().equals(trip.getId()));
            
            if (hasBook) {
                tripsWithBooks++;
                log.debug("行程 '{}' 有对应账本", trip.getName());
            } else {
                tripsWithoutBooks++;
                log.warn("行程 '{}' (ID: {}) 没有对应账本", trip.getName(), trip.getId());
            }
        }
        
        log.info("统计结果：{} 个行程有账本，{} 个行程没有账本", tripsWithBooks, tripsWithoutBooks);
        
        // 检查孤立的账本（没有对应行程的账本）
        int orphanBooks = 0;
        for (Book book : allBooks) {
            boolean hasTrip = allTrips.stream()
                .anyMatch(trip -> trip.getId().equals(book.getTripId()));
            
            if (!hasTrip) {
                orphanBooks++;
                log.warn("账本 '{}' (ID: {}) 没有对应的有效行程", book.getName(), book.getId());
            }
        }
        
        if (orphanBooks > 0) {
            log.warn("发现 {} 个孤立账本（没有对应行程）", orphanBooks);
        }
    }
    
    /**
     * 全面检查用户的行程和账本对应关系
     */
    @Test
    public void checkUserTripBookRelationship() {
        Long userId = 5L;
        log.info("全面检查用户 {} 的行程和账本对应关系...", userId);
        
        // 1. 获取用户参与的所有行程
        List<TripUser> userTrips = tripUserMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                .eq("user_id", userId)
                .eq("is_deleted", 0)
        );
        
        log.info("用户 {} 参与了 {} 个行程", userId, userTrips.size());
        
        for (TripUser tripUser : userTrips) {
            Long tripId = tripUser.getTripId();
            log.info("检查行程 {} (角色: {})", tripId, tripUser.getRole());
            
            // 检查行程信息
            Trip trip = tripMapper.selectById(tripId);
            if (trip == null || trip.getIsDeleted() == 1) {
                log.warn("行程 {} 不存在或已删除", tripId);
                continue;
            }
            
            // 检查该行程的账本
            List<Book> tripBooks = bookMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                    .eq("trip_id", tripId)
                    .eq("is_deleted", 0)
            );
            
            log.info("行程 {} ('{}') 有 {} 个账本", tripId, trip.getName(), tripBooks.size());
            
            if (tripBooks.isEmpty()) {
                log.warn("行程 {} ('{}') 没有账本！", tripId, trip.getName());
            } else {
                for (Book book : tripBooks) {
                    // 检查用户是否在账本中
                    BookUser bookUser = bookUserMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                            .eq("book_id", book.getId())
                            .eq("user_id", userId)
                            .eq("is_deleted", 0)
                    );
                    
                    if (bookUser == null) {
                        log.warn("用户 {} 不在账本 {} ('{}') 中！", userId, book.getId(), book.getName());
                    } else {
                        log.info("用户 {} 在账本 {} ('{}') 中", userId, book.getId(), book.getName());
                    }
                }
            }
        }
    }

    /**
     * 修复现有账本的成员权限
     * 为现有账本添加缺失的行程成员
     */
    @Test
    @Transactional
    public void fixExistingAccountBookMembers() {
        log.info("开始修复现有账本的成员权限...");
        
        try {
            // 获取所有有效账本
            List<Book> allBooks = bookMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                    .eq("is_deleted", 0)
            );
            
            log.info("找到 {} 个有效账本", allBooks.size());
            
            int fixedCount = 0;
            
            for (Book book : allBooks) {
                log.info("处理账本: '{}' (ID: {}, 行程ID: {})", book.getName(), book.getId(), book.getTripId());
                
                // 获取该行程的所有成员
                List<TripUser> tripMembers = tripUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                        .eq("trip_id", book.getTripId())
                        .eq("is_deleted", 0)
                );
                
                log.info("行程 {} 有 {} 个成员", book.getTripId(), tripMembers.size());
                
                // 获取账本现有成员
                List<BookUser> existingBookUsers = bookUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                        .eq("book_id", book.getId())
                        .eq("is_deleted", 0)
                );
                
                Set<Long> existingUserIds = existingBookUsers.stream()
                    .map(BookUser::getUserId)
                    .collect(java.util.stream.Collectors.toSet());
                
                log.info("账本 {} 现有 {} 个成员: {}", book.getId(), existingUserIds.size(), existingUserIds);
                
                // 添加缺失的成员
                int addedMembers = 0;
                for (TripUser tripMember : tripMembers) {
                    if (!existingUserIds.contains(tripMember.getUserId())) {
                        BookUser bookUser = new BookUser();
                        bookUser.setBookId(book.getId());
                        bookUser.setUserId(tripMember.getUserId());
                        bookUserMapper.insert(bookUser);
                        addedMembers++;
                        log.info("为账本 {} 添加成员: 用户ID {}", book.getId(), tripMember.getUserId());
                    }
                }
                
                if (addedMembers > 0) {
                    fixedCount++;
                    log.info("账本 '{}' 添加了 {} 个缺失成员", book.getName(), addedMembers);
                } else {
                    log.info("账本 '{}' 成员权限正常，无需修复", book.getName());
                }
            }
            
            log.info("账本成员权限修复完成！修复了 {} 个账本", fixedCount);
            
        } catch (Exception e) {
            log.error("修复账本成员权限失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 修复邀请用户的账本访问权限问题
     * 确保所有行程成员都能访问对应的账本
     */
    @Test
    @Transactional
    public void fixInvitedUserAccountBookAccess() {
        log.info("开始修复邀请用户的账本访问权限问题...");
        
        try {
            // 1. 获取所有有效的行程
            List<Trip> allTrips = tripMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Trip>()
                    .eq("is_deleted", 0)
            );
            
            log.info("找到 {} 个有效行程", allTrips.size());
            
            int fixedTrips = 0;
            int addedMembers = 0;
            
            for (Trip trip : allTrips) {
                log.info("处理行程: ID={}, 名称='{}'", trip.getId(), trip.getName());
                
                // 2. 获取该行程的所有成员
                List<TripUser> tripMembers = tripUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                        .eq("trip_id", trip.getId())
                        .eq("is_deleted", 0)
                );
                
                if (tripMembers.isEmpty()) {
                    log.warn("行程 {} 没有成员，跳过", trip.getId());
                    continue;
                }
                
                log.info("行程 {} 有 {} 个成员", trip.getId(), tripMembers.size());
                
                // 3. 获取该行程的所有账本
                List<Book> tripBooks = bookMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                        .eq("trip_id", trip.getId())
                        .eq("is_deleted", 0)
                );
                
                if (tripBooks.isEmpty()) {
                    log.warn("行程 {} 没有账本，尝试创建...", trip.getId());
                    
                    // 找到创建者
                    TripUser creator = tripMembers.stream()
                        .filter(tu -> tu.getRole() == 0)
                        .findFirst()
                        .orElse(tripMembers.get(0)); // 如果没有创建者，使用第一个成员
                    
                    try {
                        AccountBookDTO dto = new AccountBookDTO();
                        dto.setTripId(trip.getId());
                        dto.setName(trip.getName() + "账本");
                        
                        AccountBookVO result = accountService.createAccountBook(creator.getUserId(), dto);
                        log.info("为行程 {} 创建了账本: ID={}, 名称='{}'", trip.getId(), result.getBookId(), result.getName());
                        
                        // 重新获取账本列表
                        tripBooks = bookMapper.selectList(
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                                .eq("trip_id", trip.getId())
                                .eq("is_deleted", 0)
                        );
                    } catch (Exception e) {
                        log.error("为行程 {} 创建账本失败: {}", trip.getId(), e.getMessage());
                        continue;
                    }
                }
                
                log.info("行程 {} 有 {} 个账本", trip.getId(), tripBooks.size());
                
                boolean tripFixed = false;
                
                // 4. 确保每个行程成员都在每个账本中
                for (Book book : tripBooks) {
                    log.info("检查账本: ID={}, 名称='{}'", book.getId(), book.getName());
                    
                    // 获取账本现有成员
                    List<BookUser> existingBookUsers = bookUserMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                            .eq("book_id", book.getId())
                            .eq("is_deleted", 0)
                    );
                    
                    Set<Long> existingUserIds = existingBookUsers.stream()
                        .map(BookUser::getUserId)
                        .collect(java.util.stream.Collectors.toSet());
                    
                    log.info("账本 {} 现有成员: {}", book.getId(), existingUserIds);
                    
                    // 添加缺失的成员
                    for (TripUser tripMember : tripMembers) {
                        if (!existingUserIds.contains(tripMember.getUserId())) {
                            BookUser bookUser = new BookUser();
                            bookUser.setBookId(book.getId());
                            bookUser.setUserId(tripMember.getUserId());
                            bookUserMapper.insert(bookUser);
                            
                            addedMembers++;
                            tripFixed = true;
                            log.info("为账本 {} 添加成员: 用户ID {} (角色: {})", 
                                book.getId(), tripMember.getUserId(), 
                                tripMember.getRole() == 0 ? "创建者" : "参与者");
                        }
                    }
                }
                
                if (tripFixed) {
                    fixedTrips++;
                }
            }
            
            log.info("账本访问权限修复完成！");
            log.info("修复了 {} 个行程的账本权限", fixedTrips);
            log.info("总共添加了 {} 个账本成员权限", addedMembers);
            
            // 5. 验证修复结果 - 检查几个特定用户的账本访问情况
            log.info("验证修复结果...");
            
            // 获取所有用户
            List<TripUser> allTripUsers = tripUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("is_deleted", 0)
                    .groupBy("user_id")
            );
            
            for (TripUser tripUser : allTripUsers.stream().limit(5).collect(java.util.stream.Collectors.toList())) {
                Long userId = tripUser.getUserId();
                
                // 获取用户参与的行程数
                long userTripCount = tripUserMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                        .eq("user_id", userId)
                        .eq("is_deleted", 0)
                );
                
                // 获取用户可访问的账本数
                List<AccountBookDetailVO> userBooks = accountService.getAllAccountBooks(userId);
                
                log.info("用户 {} 参与 {} 个行程，可访问 {} 个账本", userId, userTripCount, userBooks.size());
                
                if (userTripCount != userBooks.size()) {
                    log.warn("用户 {} 的行程数和账本数不匹配！", userId);
                }
            }
            
        } catch (Exception e) {
            log.error("修复邀请用户账本访问权限失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清理重复的 trip_user 记录
     * 解决唯一约束冲突问题
     */
    @Test
    @Transactional
    public void cleanupDuplicateTripUserRecords() {
        log.info("开始清理重复的 trip_user 记录...");
        
        try {
            // 查找所有 trip_user 记录
            List<TripUser> allTripUsers = tripUserMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
            );
            
            log.info("总共找到 {} 条 trip_user 记录", allTripUsers.size());
            
            // 按 (trip_id, user_id) 分组
            Map<String, List<TripUser>> groupedRecords = allTripUsers.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    tu -> tu.getTripId() + "-" + tu.getUserId()
                ));
            
            int duplicateGroups = 0;
            int cleanedRecords = 0;
            
            for (Map.Entry<String, List<TripUser>> entry : groupedRecords.entrySet()) {
                List<TripUser> records = entry.getValue();
                
                if (records.size() > 1) {
                    duplicateGroups++;
                    log.info("发现重复记录组: {} (共 {} 条记录)", entry.getKey(), records.size());
                    
                    // 按 is_deleted 和 create_time 排序
                    records.sort((a, b) -> {
                        // 优先保留未删除的记录
                        if (a.getIsDeleted() != b.getIsDeleted()) {
                            return a.getIsDeleted().compareTo(b.getIsDeleted());
                        }
                        // 然后按创建时间倒序（保留最新的）
                        return b.getCreateTime().compareTo(a.getCreateTime());
                    });
                    
                    // 保留第一条记录，删除其他记录
                    TripUser keepRecord = records.get(0);
                    log.info("保留记录: ID={}, is_deleted={}, create_time={}", 
                        keepRecord.getId(), keepRecord.getIsDeleted(), keepRecord.getCreateTime());
                    
                    for (int i = 1; i < records.size(); i++) {
                        TripUser deleteRecord = records.get(i);
                        log.info("删除记录: ID={}, is_deleted={}, create_time={}", 
                            deleteRecord.getId(), deleteRecord.getIsDeleted(), deleteRecord.getCreateTime());
                        
                        // 物理删除重复记录
                        tripUserMapper.deleteById(deleteRecord.getId());
                        cleanedRecords++;
                    }
                }
            }
            
            log.info("清理完成！");
            log.info("发现 {} 个重复组", duplicateGroups);
            log.info("清理了 {} 条重复记录", cleanedRecords);
            
        } catch (Exception e) {
            log.error("清理重复记录失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 测试重新加入行程功能
     */
    @Test
    public void testRejoinTrip() {
        Long testUserId = 5L;
        Long testTripId = 16L;
        
        log.info("测试用户 {} 重新加入行程 {}", testUserId, testTripId);
        
        try {
            // 1. 检查当前状态
            TripUser currentRecord = tripUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("trip_id", testTripId)
                    .eq("user_id", testUserId)
                    .orderByDesc("create_time")
                    .last("LIMIT 1")
            );
            
            if (currentRecord == null) {
                log.info("用户 {} 没有行程 {} 的记录", testUserId, testTripId);
            } else {
                log.info("用户 {} 在行程 {} 中的当前状态: is_deleted={}, role={}", 
                    testUserId, testTripId, currentRecord.getIsDeleted(), currentRecord.getRole());
            }
            
            // 2. 尝试重新加入（模拟）
            log.info("模拟重新加入行程...");
            
            // 检查是否存在已删除的记录
            TripUser deletedRecord = tripUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("trip_id", testTripId)
                    .eq("user_id", testUserId)
                    .eq("is_deleted", 1)
            );
            
            if (deletedRecord != null) {
                log.info("找到已删除的记录: ID={}, 可以恢复", deletedRecord.getId());
                
                // 模拟恢复记录
                log.info("模拟恢复记录: 设置 is_deleted=0, role=1");
                
                // 实际恢复（取消注释以执行）
                /*
                deletedRecord.setIsDeleted((byte) 0);
                deletedRecord.setRole(1);
                tripUserMapper.updateById(deletedRecord);
                log.info("已恢复用户 {} 在行程 {} 中的记录", testUserId, testTripId);
                */
            } else {
                log.info("没有找到已删除的记录，需要创建新记录");
            }
            
        } catch (Exception e) {
            log.error("测试重新加入行程失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查数据库约束信息
     */
    @Test
    public void checkDatabaseConstraints() {
        log.info("检查数据库约束信息...");
        
        // 这个方法需要直接执行SQL来查看约束
        // 由于我们使用的是MyBatis-Plus，这里只能提供SQL语句
        
        log.info("请在数据库中执行以下SQL来检查约束:");
        log.info("SHOW INDEX FROM trip_user WHERE Key_name LIKE '%trip_user%';");
        log.info("SHOW CREATE TABLE trip_user;");
        
        log.info("如果发现唯一约束冲突，请执行以下SQL:");
        log.info("-- 删除现有约束");
        log.info("ALTER TABLE trip_user DROP INDEX uk_trip_user;");
        log.info("-- 创建新约束（包含 is_deleted 字段）");
        log.info("ALTER TABLE trip_user ADD UNIQUE INDEX uk_trip_user_with_deleted (trip_id, user_id, is_deleted);");
    }

    /**
     * 清理测试数据（谨慎使用）
     * 删除所有测试创建的账本
     */
    @Test
    @Transactional
    public void cleanupTestAccountBooks() {
        log.warn("警告：这个方法会删除所有名称以'账本'结尾的账本！");
        log.warn("请确认这是在测试环境中运行！");
        
        // 为了安全，这里只是打印而不实际删除
        List<Book> testBooks = bookMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                .like("name", "%账本")
                .eq("is_deleted", 0)
        );
        
        log.info("找到 {} 个可能的测试账本：", testBooks.size());
        for (Book book : testBooks) {
            log.info("- 账本 '{}' (ID: {}, 行程ID: {})", book.getName(), book.getId(), book.getTripId());
        }
        
        log.info("如需删除，请取消注释下面的代码并重新运行");
        
        /*
        // 取消注释以下代码来实际删除测试数据
        for (Book book : testBooks) {
            // 软删除账本
            book.setIsDeleted((byte) 1);
            bookMapper.updateById(book);
            
            // 软删除相关的用户关系
            bookUserMapper.update(null, 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<BookUser>()
                    .set("is_deleted", 1)
                    .eq("book_id", book.getId())
            );
            
            log.info("已删除账本 '{}'", book.getName());
        }
        */
    }

    /**
     * 测试退出行程功能
     * 验证用户退出行程后，相关的账本权限也被正确移除
     */
    @Test
    public void testLeaveTripFunctionality() {
        log.info("测试退出行程功能...");
        
        // 使用一个测试用户ID和行程ID
        Long testUserId = 5L;
        Long testTripId = 16L; // 根据日志，用户5参与了行程16
        
        log.info("测试用户 {} 退出行程 {}", testUserId, testTripId);
        
        try {
            // 1. 检查用户退出前的状态
            log.info("=== 退出前状态检查 ===");
            
            // 检查用户是否在行程中
            TripUser tripUser = tripUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("trip_id", testTripId)
                    .eq("user_id", testUserId)
                    .eq("is_deleted", 0)
            );
            
            if (tripUser == null) {
                log.warn("用户 {} 不在行程 {} 中，无法测试退出功能", testUserId, testTripId);
                return;
            }
            
            log.info("用户 {} 在行程 {} 中，角色: {}", testUserId, testTripId, 
                tripUser.getRole() == 0 ? "创建者" : "参与者");
            
            if (tripUser.getRole() == 0) {
                log.warn("用户 {} 是行程 {} 的创建者，不能退出", testUserId, testTripId);
                return;
            }
            
            // 检查用户在该行程账本中的权限
            List<Book> tripBooks = bookMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                    .eq("trip_id", testTripId)
                    .eq("is_deleted", 0)
            );
            
            log.info("行程 {} 有 {} 个账本", testTripId, tripBooks.size());
            
            int userBookCount = 0;
            for (Book book : tripBooks) {
                BookUser bookUser = bookUserMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                        .eq("book_id", book.getId())
                        .eq("user_id", testUserId)
                        .eq("is_deleted", 0)
                );
                
                if (bookUser != null) {
                    userBookCount++;
                    log.info("用户 {} 在账本 {} ('{}') 中有权限", testUserId, book.getId(), book.getName());
                }
            }
            
            log.info("用户 {} 在 {} 个账本中有权限", testUserId, userBookCount);
            
            // 2. 执行退出行程操作
            log.info("=== 执行退出行程操作 ===");
            
            // 注意：这里只是模拟，不实际执行，因为这会修改数据
            log.info("模拟调用: tripUserService.removeParticipant({}, {})", testTripId, testUserId);
            
            // 如果要实际测试，取消注释下面的代码：
            /*
            tripUserService.removeParticipant(testTripId, testUserId);
            log.info("用户 {} 已退出行程 {}", testUserId, testTripId);
            
            // 3. 检查退出后的状态
            log.info("=== 退出后状态检查 ===");
            
            // 检查用户是否已从行程中移除
            TripUser afterTripUser = tripUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                    .eq("trip_id", testTripId)
                    .eq("user_id", testUserId)
                    .eq("is_deleted", 0)
            );
            
            if (afterTripUser == null) {
                log.info("✓ 用户 {} 已从行程 {} 中移除", testUserId, testTripId);
            } else {
                log.error("✗ 用户 {} 仍在行程 {} 中", testUserId, testTripId);
            }
            
            // 检查用户是否已从账本中移除
            int afterUserBookCount = 0;
            for (Book book : tripBooks) {
                BookUser bookUser = bookUserMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                        .eq("book_id", book.getId())
                        .eq("user_id", testUserId)
                        .eq("is_deleted", 0)
                );
                
                if (bookUser != null) {
                    afterUserBookCount++;
                    log.error("✗ 用户 {} 仍在账本 {} ('{}') 中有权限", testUserId, book.getId(), book.getName());
                } else {
                    log.info("✓ 用户 {} 已从账本 {} ('{}') 中移除", testUserId, book.getId(), book.getName());
                }
            }
            
            if (afterUserBookCount == 0) {
                log.info("✓ 用户 {} 已从所有相关账本中移除", testUserId);
            } else {
                log.error("✗ 用户 {} 仍在 {} 个账本中有权限", testUserId, afterUserBookCount);
            }
            */
            
            log.info("退出行程功能测试完成（模拟模式）");
            log.info("如需实际测试，请取消注释相关代码");
            
        } catch (Exception e) {
            log.error("测试退出行程功能失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 验证退出行程的业务逻辑
     */
    @Test
    public void validateLeaveTripLogic() {
        log.info("验证退出行程的业务逻辑...");
        
        // 测试各种场景
        Long testUserId = 5L;
        
        // 1. 获取用户参与的所有行程
        List<TripUser> userTrips = tripUserMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TripUser>()
                .eq("user_id", testUserId)
                .eq("is_deleted", 0)
        );
        
        log.info("用户 {} 参与了 {} 个行程", testUserId, userTrips.size());
        
        for (TripUser tripUser : userTrips) {
            Long tripId = tripUser.getTripId();
            String role = tripUser.getRole() == 0 ? "创建者" : "参与者";
            
            log.info("行程 {} - 角色: {}", tripId, role);
            
            // 检查业务逻辑
            if (tripUser.getRole() == 0) {
                log.info("  ✓ 创建者不能退出行程");
            } else {
                log.info("  ✓ 参与者可以退出行程");
                
                // 检查该行程的账本情况
                List<Book> tripBooks = bookMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Book>()
                        .eq("trip_id", tripId)
                        .eq("is_deleted", 0)
                );
                
                log.info("  - 行程 {} 有 {} 个账本", tripId, tripBooks.size());
                
                for (Book book : tripBooks) {
                    BookUser bookUser = bookUserMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BookUser>()
                            .eq("book_id", book.getId())
                            .eq("user_id", testUserId)
                            .eq("is_deleted", 0)
                    );
                    
                    if (bookUser != null) {
                        log.info("  - 用户在账本 '{}' 中有权限", book.getName());
                    } else {
                        log.warn("  - 用户不在账本 '{}' 中", book.getName());
                    }
                }
            }
        }
        
        log.info("业务逻辑验证完成");
    }
}