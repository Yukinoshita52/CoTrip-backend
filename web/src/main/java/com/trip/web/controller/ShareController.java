package com.trip.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.common.result.Result;
import com.trip.model.entity.Share;
import com.trip.web.service.ShareService;
import kotlin.ReplaceWith;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: ShareController
 * Package: com.trip.web.controller
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/10/21 23:36
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {
    @Autowired
    private final ShareService shareService;

    @PostMapping("/{tripId}")
    public Result<Void> shareTrip(@PathVariable Long tripId) {
        shareService.insertShare(tripId);
        return Result.ok();
    }

    /**
     * 分页查询分享行程
     * 必传参数：
     *  - page: 当前页码（默认 1）
     *  - page_size: 每页数量（默认 10）
     * 可选参数：
     *  - user_id: 用户ID（按用户过滤）
     *  - keyword: 行程名模糊搜索
     *  - sort_by: 排序字段（默认 created_time 降序）
     */
    @GetMapping("/list")
    public IPage<Share> listShares(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort_by", defaultValue = "created_time") String sortBy) {

        // 构建分页对象
        Page<Share> pageInfo = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Share> query = new LambdaQueryWrapper<>();
        if (userId != null) {
            query.eq(Share::getTripId, userId);
        }
        //这里使用keyword对所有分享的行程进行模糊查询
//        if (StringUtils.hasText(keyword)) {
//            query.like(Share::getTripName, keyword);
//        }
        query.orderByDesc(Share::getCreateTime);

        // 排序字段可动态处理
        if (!"create_time".equalsIgnoreCase(sortBy)) {
            query.last("ORDER BY " + sortBy + " DESC");
        }

        // 分页查询
        return shareService.page(pageInfo, query);
    }
}
