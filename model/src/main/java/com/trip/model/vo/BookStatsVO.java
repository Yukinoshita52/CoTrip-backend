package com.trip.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ClassName: BookStatsVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 13:19
 * @Version 1.0
 */
@Data
public class BookStatsVO {
    private Long bookId;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private List<CategoryStat> categoryStats;
    private List<DailyStat> dailyStats;

    @Data
    public static class CategoryStat {
        private String categoryName;
        private BigDecimal expense = BigDecimal.ZERO;
    }

    @Data
    public static class DailyStat {
        private Date date;
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
    }
}
