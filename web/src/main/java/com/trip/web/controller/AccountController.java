package com.trip.web.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.trip.common.login.LoginUser;
import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.model.dto.AccountBookDTO;
import com.trip.model.dto.RecordDTO;
import com.trip.model.entity.AccountBookCategory;
import com.trip.model.entity.Book;
import com.trip.model.vo.*;
import com.trip.web.mapper.GraphInfoMapper;
import com.trip.web.service.AccountBookCategoryService;
import com.trip.web.service.AccountService;
import com.trip.web.service.GraphInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: AccountController
 * Package: com.trip.web.controller
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/29 14:33
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    @Autowired
    private final AccountService accountService;
    @Autowired
    private final AccountBookCategoryService accountBookCategoryService;
    @Autowired
    private final GraphInfoService graphInfoService;

    @PostMapping("/book")
    public Result<AccountBookVO> createAccountBook(@RequestBody AccountBookDTO dto) {
        AccountBookVO res = accountService.createAccountBook(LoginUserHolder.getLoginUser().getUserId(),dto);
        return Result.ok(res);
    }

    @GetMapping("/book/list")
    public Result<List<AccountBookDetailVO>> getAllAccountBooks() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        System.out.println("获取账本列表 - 用户ID: " + loginUser.getUserId());
        List<AccountBookDetailVO> res = accountService.getAllAccountBooks(loginUser.getUserId());
        System.out.println("返回账本数量: " + res.size());
        for (AccountBookDetailVO book : res) {
            System.out.println("账本: ID=" + book.getBookId() + ", 名称=" + book.getName() + ", 行程ID=" + book.getTripId());
        }
        return Result.ok(res);
    }

    @DeleteMapping("/book/{bookId}")
    public Result<Void> removeAccountBook(@PathVariable Long bookId) {
        accountService.removeRecordById(bookId);
        return Result.ok();
    }

    @PostMapping("/record")
    public Result<RecordVO> addRecord(@RequestBody RecordDTO record) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        RecordVO res = accountService.addRecord(loginUser.getUserId(), record);
        return Result.ok(res);
    }

    @GetMapping("/record/list")
    public Result<RecordPageVO> pageRecords(@RequestParam("bookId") Long bookId, @RequestParam("page") Integer page, @RequestParam("size") Integer size) {
        RecordPageVO res = accountService.pageRecords(bookId, page, size);
        return Result.ok(res);
    }

    @GetMapping("/record/{recordId}")
    public Result<RecordVO> getRecord(@PathVariable("recordId") Long recordId) {
        RecordVO res = accountService.getRecord(recordId);
        return Result.ok(res);
    }

    @PutMapping("/record/{recordId}")
    public Result<RecordVO> editRecord(@PathVariable("recordId") Long recordId, @RequestBody RecordDTO record) {
        RecordVO res = accountService.updateRecord(recordId, record);
        return Result.ok(res);
    }

    @DeleteMapping("record/{recordId}")
    public Result<Void> deleteRecord(@PathVariable("recordId") Long recordId) {
        accountService.removeRecordById(recordId);
        return Result.ok();
    }

    @GetMapping("/book/{bookId}/stats")
    public Result<BookStatsVO> getBookStats(@PathVariable("bookId") Long bookId) {
        BookStatsVO res = accountService.getBookStats(bookId);
        return Result.ok(res);
    }

    @GetMapping("/book/{bookId}/split")
    public Result<Map<String, Object>> splitAmount(@PathVariable Long bookId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        List<PayMemberVO> tmpRes = accountService.splitAmount(bookId, loginUser.getUserId());
        
        // 获取总成员数（包括当前用户）
        Integer totalMembers = accountService.getTotalMembersCount(bookId);
        
        Map<String, Object> res = new HashMap<>();
        res.put("members", tmpRes);
        res.put("totalMembers", totalMembers);
        
        return Result.ok(res);
    }
    
    @GetMapping("/book/{bookId}/my-expense")
    public Result<Map<String, Object>> getMyExpenseStats(@PathVariable Long bookId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        // 获取我的支出金额
        java.math.BigDecimal myExpenseAmount = accountService.getMyExpenseAmount(bookId, loginUser.getUserId());
        
        // 获取我的账单数量
        Integer myExpenseCount = accountService.getMyExpenseCount(bookId, loginUser.getUserId());
        
        Map<String, Object> res = new HashMap<>();
        res.put("myExpenseAmount", myExpenseAmount);
        res.put("myExpenseCount", myExpenseCount);
        
        return Result.ok(res);
    }

    @PostMapping("/category")
    public Result<Map<String,Long>> addAccountBookCategory(@RequestParam("name") String name,
                                               @RequestParam("type") Integer type,
                                               @RequestParam(value = "file", required = false) MultipartFile file) {
        AccountBookCategory abc = new AccountBookCategory();
        abc.setName(name);
        abc.setType(type);
        accountBookCategoryService.save(abc);
        Long graphInfoId = graphInfoService.uploadImage(file, 4, abc.getId());
        abc.setIconId(graphInfoId);
        accountBookCategoryService.update(new LambdaUpdateWrapper<AccountBookCategory>()
                // 明确设置要更新的字段
                .set(AccountBookCategory::getIconId, graphInfoId)
                // 确保更新条件是正确的ID
                .eq(AccountBookCategory::getId, abc.getId())
        );
        Map<String,Long> res = new HashMap<>();
        res.put("categoryId", abc.getId());
        return Result.ok(res);
    }
}
