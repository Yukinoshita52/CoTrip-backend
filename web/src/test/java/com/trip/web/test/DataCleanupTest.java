package com.trip.web.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 数据清理测试类
 * 清理数据库中的错乱数据，确保数据一致性
 */
@SpringBootTest
@Slf4j
public class DataCleanupTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void cleanupAllErrorData() {
        log.info("开始清理数据库错误数据...");
        
        // 按依赖关系顺序清理，先清理依赖表，再清理主表
        cleanupAccountBookRecord();
        cleanupBookUser();
        cleanupBook();
        cleanupComment();
        cleanupGraphInfo();
        cleanupInvitation();
        cleanupPost();
        cleanupPostLike();
        cleanupTripPlace();
        cleanupTripUser();
        cleanupTrip();

        log.info("数据清理完成！");
    }

    /**
     * 检查并修复账单类别数据
     * 确保所有account_book_record引用的category_id都存在对应的account_book_category记录
     */
    @Test
    public void checkAndFixCategoryData() {
        log.info("开始检查账单类别数据...");
        
        // 1. 查找所有缺失的类别ID
        String findMissingCategoriesSQL = """
            SELECT DISTINCT abr.category_id 
            FROM account_book_record abr 
            LEFT JOIN account_book_category abc ON abr.category_id = abc.id AND abc.is_deleted = 0
            WHERE abr.is_deleted = 0 AND abc.id IS NULL
            """;
        
        List<Map<String, Object>> missingCategories = jdbcTemplate.queryForList(findMissingCategoriesSQL);
        log.info("发现 {} 个缺失的类别ID", missingCategories.size());
        
        for (Map<String, Object> row : missingCategories) {
            Long categoryId = ((Number) row.get("category_id")).longValue();
            log.info("缺失的类别ID: {}", categoryId);
            
            // 2. 为缺失的类别ID创建默认类别记录
            String insertCategorySQL = """
                INSERT INTO account_book_category (id, name, icon_id, is_deleted, create_time, update_time) 
                VALUES (?, '其他', 1, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE is_deleted = 0, update_time = NOW()
                """;
            
            try {
                jdbcTemplate.update(insertCategorySQL, categoryId);
                log.info("已创建/修复类别ID: {}", categoryId);
            } catch (Exception e) {
                log.error("创建类别ID {} 失败: {}", categoryId, e.getMessage());
            }
        }
        
        // 3. 验证修复结果
        List<Map<String, Object>> stillMissingCategories = jdbcTemplate.queryForList(findMissingCategoriesSQL);
        if (stillMissingCategories.isEmpty()) {
            log.info("所有类别数据已修复完成！");
        } else {
            log.warn("仍有 {} 个类别ID未修复", stillMissingCategories.size());
        }
        
        // 4. 显示所有账单记录的类别信息
        String checkRecordsSQL = """
            SELECT abr.id, abr.category_id, abr.remark, abc.name as category_name
            FROM account_book_record abr 
            LEFT JOIN account_book_category abc ON abr.category_id = abc.id AND abc.is_deleted = 0
            WHERE abr.is_deleted = 0
            ORDER BY abr.id
            """;
        
        List<Map<String, Object>> allRecords = jdbcTemplate.queryForList(checkRecordsSQL);
        log.info("所有账单记录的类别信息:");
        for (Map<String, Object> record : allRecords) {
            log.info("记录ID: {}, 类别ID: {}, 类别名称: {}, 备注: '{}'", 
                record.get("id"), 
                record.get("category_id"), 
                record.get("category_name"),
                record.get("remark"));
        }
    }

    /**
     * 1. 清理trip表
     * 对于每一条trip，都应该有其创建者，也就是在trip_user表中，存在一条记录trip.id = trip_user.trip_id、role=0
     */
    @Test
    public void cleanupTrip() {
        log.info("开始清理trip表...");
        
        String sql = """
            DELETE FROM trip 
            WHERE id NOT IN (
                SELECT DISTINCT trip_id 
                FROM trip_user 
                WHERE role = 0 AND is_deleted = 0
            )
            """;
        
        int deletedCount = jdbcTemplate.update(sql);
        log.info("清理trip表完成，删除了 {} 条没有创建者的行程记录", deletedCount);
    }

    /**
     * 2. 清理trip_user表
     * 如果trip表中没有这个id或者user表中没有这个user_id就清理
     */
    @Test
    public void cleanupTripUser() {
        log.info("开始清理trip_user表...");
        
        // 清理没有对应trip的记录
        String sql1 = """
            DELETE FROM trip_user 
            WHERE trip_id NOT IN (
                SELECT id FROM trip WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理trip_user表中没有对应trip的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM trip_user 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理trip_user表中没有对应user的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 3. 清理trip_place表
     * 如果没有trip或者没有place就清理
     */
    @Test
    public void cleanupTripPlace() {
        log.info("开始清理trip_place表...");
        
        // 清理没有对应trip的记录
        String sql1 = """
            DELETE FROM trip_place 
            WHERE trip_id NOT IN (
                SELECT id FROM trip WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理trip_place表中没有对应trip的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应place的记录
        String sql2 = """
            DELETE FROM trip_place 
            WHERE place_id NOT IN (
                SELECT id FROM place WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理trip_place表中没有对应place的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 4. 清理post_like表
     * 如果没有post或者user就清理
     */
    @Test
    public void cleanupPostLike() {
        log.info("开始清理post_like表...");
        
        // 清理没有对应post的记录
        String sql1 = """
            DELETE FROM post_like 
            WHERE post_id NOT IN (
                SELECT id FROM post WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理post_like表中没有对应post的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM post_like 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理post_like表中没有对应user的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 5. 清理post表
     * 如果没有trip或者user就清理
     */
    @Test
    public void cleanupPost() {
        log.info("开始清理post表...");
        
        // 清理没有对应trip的记录
        String sql1 = """
            DELETE FROM post 
            WHERE trip_id NOT IN (
                SELECT id FROM trip WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理post表中没有对应trip的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM post 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理post表中没有对应user的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 6. 清理invitation表
     * 根据实际表结构调整清理逻辑
     */
    @Test
    public void cleanupInvitation() {
        log.info("开始清理invitation表...");
        
        // 根据MySQL表结构.md，invitation表只有invitee字段（被邀请人手机号）
        // 如果有其他关联字段，需要根据实际情况调整
        
        // 检查表结构，获取所有列名
        try {
            String checkColumnsSql = "SHOW COLUMNS FROM invitation";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkColumnsSql);
            
            boolean hasTripId = false;
            boolean hasInviterId = false;
            
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("Field");
                if ("trip_id".equals(columnName)) {
                    hasTripId = true;
                }
                if ("inviter_id".equals(columnName)) {
                    hasInviterId = true;
                }
            }
            
            log.info("invitation表结构检查: hasTripId={}, hasInviterId={}", hasTripId, hasInviterId);
            
            // 根据实际字段进行清理
            if (hasTripId) {
                String sql1 = """
                    DELETE FROM invitation 
                    WHERE trip_id NOT IN (
                        SELECT id FROM trip WHERE is_deleted = 0
                    )
                    """;
                int deletedCount1 = jdbcTemplate.update(sql1);
                log.info("清理invitation表中没有对应trip的记录，删除了 {} 条", deletedCount1);
            }
            
            if (hasInviterId) {
                String sql2 = """
                    DELETE FROM invitation 
                    WHERE inviter_id NOT IN (
                        SELECT id FROM user WHERE is_deleted = 0
                    )
                    """;
                int deletedCount2 = jdbcTemplate.update(sql2);
                log.info("清理invitation表中没有对应inviter的记录，删除了 {} 条", deletedCount2);
            }
            
            if (!hasTripId && !hasInviterId) {
                log.info("invitation表没有trip_id和inviter_id字段，跳过关联清理");
            }
            
        } catch (Exception e) {
            log.error("清理invitation表时出错: {}", e.getMessage());
        }
    }

    /**
     * 7. 清理graph_info表
     * 根据item_type和item_id进行清理
     * item_type含义：1-用户头像，2-行程图片，3-地点图片，4-其他（account_book_category，不检查）
     */
    @Test
    public void cleanupGraphInfo() {
        log.info("开始清理graph_info表...");
        
        // 清理item_type=1（用户头像）但用户不存在的记录
        String sql1 = """
            DELETE FROM graph_info 
            WHERE item_type = 1 
            AND item_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理graph_info表中用户头像但用户不存在的记录，删除了 {} 条", deletedCount1);
        
        // 清理item_type=2（行程图片）但行程不存在的记录
        String sql2 = """
            DELETE FROM graph_info 
            WHERE item_type = 2 
            AND item_id NOT IN (
                SELECT id FROM trip WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理graph_info表中行程图片但行程不存在的记录，删除了 {} 条", deletedCount2);
        
        // 清理item_type=3（地点图片）但地点不存在的记录
        String sql3 = """
            DELETE FROM graph_info 
            WHERE item_type = 3 
            AND item_id NOT IN (
                SELECT id FROM place WHERE is_deleted = 0
            )
            """;
        int deletedCount3 = jdbcTemplate.update(sql3);
        log.info("清理graph_info表中地点图片但地点不存在的记录，删除了 {} 条", deletedCount3);
        
        // item_type=4（account_book_category）不检查，按要求跳过
        log.info("跳过item_type=4（account_book_category）的清理");
    }

    /**
     * 8. 清理comment表
     * 如果没有post或user，就清理
     */
    @Test
    public void cleanupComment() {
        log.info("开始清理comment表...");
        
        // 清理没有对应post的记录
        String sql1 = """
            DELETE FROM comment 
            WHERE post_id NOT IN (
                SELECT id FROM post WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理comment表中没有对应post的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM comment 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理comment表中没有对应user的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 9. 清理book_user表
     * 如果没有book或user，就清理
     */
    @Test
    public void cleanupBookUser() {
        log.info("开始清理book_user表...");
        
        // 清理没有对应book的记录
        String sql1 = """
            DELETE FROM book_user 
            WHERE book_id NOT IN (
                SELECT id FROM book WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理book_user表中没有对应book的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM book_user 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理book_user表中没有对应user的记录，删除了 {} 条", deletedCount2);
    }

    /**
     * 10. 清理book表
     * 如果没有对应的trip就清理
     */
    @Test
    public void cleanupBook() {
        log.info("开始清理book表...");
        
        String sql = """
            DELETE FROM book 
            WHERE trip_id NOT IN (
                SELECT id FROM trip WHERE is_deleted = 0
            )
            """;
        
        int deletedCount = jdbcTemplate.update(sql);
        log.info("清理book表中没有对应trip的记录，删除了 {} 条", deletedCount);
    }

    /**
     * 11. 清理account_book_record表
     * 如果没有book、user或category就清理
     */
    @Test
    public void cleanupAccountBookRecord() {
        log.info("开始清理account_book_record表...");
        
        // 清理没有对应book的记录
        String sql1 = """
            DELETE FROM account_book_record 
            WHERE book_id NOT IN (
                SELECT id FROM book WHERE is_deleted = 0
            )
            """;
        int deletedCount1 = jdbcTemplate.update(sql1);
        log.info("清理account_book_record表中没有对应book的记录，删除了 {} 条", deletedCount1);
        
        // 清理没有对应user的记录
        String sql2 = """
            DELETE FROM account_book_record 
            WHERE user_id NOT IN (
                SELECT id FROM user WHERE is_deleted = 0
            )
            """;
        int deletedCount2 = jdbcTemplate.update(sql2);
        log.info("清理account_book_record表中没有对应user的记录，删除了 {} 条", deletedCount2);
        
        // 清理没有对应category的记录
        String sql3 = """
            DELETE FROM account_book_record 
            WHERE category_id NOT IN (
                SELECT id FROM account_book_category WHERE is_deleted = 0
            )
            """;
        int deletedCount3 = jdbcTemplate.update(sql3);
        log.info("清理account_book_record表中没有对应category的记录，删除了 {} 条", deletedCount3);
    }

    /**
     * 数据一致性检查
     * 检查清理后的数据是否还有不一致的情况
     */
    @Test
    public void checkDataConsistency() {
        log.info("开始检查数据一致性...");
        
        int totalInconsistencies = 0;
        
        // 1. 检查trip表是否都有创建者
        String sql1 = """
            SELECT COUNT(*) FROM trip t 
            WHERE t.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM trip_user tu 
                WHERE tu.trip_id = t.id AND tu.role = 0 AND tu.is_deleted = 0
            )
            """;
        Integer orphanTrips = jdbcTemplate.queryForObject(sql1, Integer.class);
        log.info("没有创建者的行程数量: {}", orphanTrips);
        totalInconsistencies += orphanTrips;
        
        // 2. 检查trip_user表的一致性
        String sql2 = """
            SELECT COUNT(*) FROM trip_user tu 
            WHERE tu.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM trip t 
                WHERE t.id = tu.trip_id AND t.is_deleted = 0
            )
            """;
        Integer orphanTripUsers = jdbcTemplate.queryForObject(sql2, Integer.class);
        log.info("引用不存在行程的trip_user记录数量: {}", orphanTripUsers);
        totalInconsistencies += orphanTripUsers;
        
        String sql3 = """
            SELECT COUNT(*) FROM trip_user tu 
            WHERE tu.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = tu.user_id AND u.is_deleted = 0
            )
            """;
        Integer tripUsersWithoutUser = jdbcTemplate.queryForObject(sql3, Integer.class);
        log.info("引用不存在用户的trip_user记录数量: {}", tripUsersWithoutUser);
        totalInconsistencies += tripUsersWithoutUser;
        
        // 3. 检查trip_place表的一致性
        String sql4 = """
            SELECT COUNT(*) FROM trip_place tp 
            WHERE tp.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM trip t 
                WHERE t.id = tp.trip_id AND t.is_deleted = 0
            )
            """;
        Integer orphanTripPlaces = jdbcTemplate.queryForObject(sql4, Integer.class);
        log.info("引用不存在行程的trip_place记录数量: {}", orphanTripPlaces);
        totalInconsistencies += orphanTripPlaces;
        
        String sql5 = """
            SELECT COUNT(*) FROM trip_place tp 
            WHERE tp.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM place p 
                WHERE p.id = tp.place_id AND p.is_deleted = 0
            )
            """;
        Integer tripPlacesWithoutPlace = jdbcTemplate.queryForObject(sql5, Integer.class);
        log.info("引用不存在地点的trip_place记录数量: {}", tripPlacesWithoutPlace);
        totalInconsistencies += tripPlacesWithoutPlace;
        
        // 4. 检查post表的一致性
        String sql6 = """
            SELECT COUNT(*) FROM post p 
            WHERE p.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM trip t 
                WHERE t.id = p.trip_id AND t.is_deleted = 0
            )
            """;
        Integer orphanPosts = jdbcTemplate.queryForObject(sql6, Integer.class);
        log.info("引用不存在行程的post记录数量: {}", orphanPosts);
        totalInconsistencies += orphanPosts;
        
        String sql7 = """
            SELECT COUNT(*) FROM post p 
            WHERE p.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = p.user_id AND u.is_deleted = 0
            )
            """;
        Integer postsWithoutUser = jdbcTemplate.queryForObject(sql7, Integer.class);
        log.info("引用不存在用户的post记录数量: {}", postsWithoutUser);
        totalInconsistencies += postsWithoutUser;
        
        // 5. 检查post_like表的一致性
        String sql8 = """
            SELECT COUNT(*) FROM post_like pl 
            WHERE pl.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM post p 
                WHERE p.id = pl.post_id AND p.is_deleted = 0
            )
            """;
        Integer orphanPostLikes = jdbcTemplate.queryForObject(sql8, Integer.class);
        log.info("引用不存在帖子的post_like记录数量: {}", orphanPostLikes);
        totalInconsistencies += orphanPostLikes;
        
        String sql9 = """
            SELECT COUNT(*) FROM post_like pl 
            WHERE pl.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = pl.user_id AND u.is_deleted = 0
            )
            """;
        Integer postLikesWithoutUser = jdbcTemplate.queryForObject(sql9, Integer.class);
        log.info("引用不存在用户的post_like记录数量: {}", postLikesWithoutUser);
        totalInconsistencies += postLikesWithoutUser;
        
        // 6. 检查comment表的一致性
        String sql10 = """
            SELECT COUNT(*) FROM comment c 
            WHERE c.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM post p 
                WHERE p.id = c.post_id AND p.is_deleted = 0
            )
            """;
        Integer orphanComments = jdbcTemplate.queryForObject(sql10, Integer.class);
        log.info("引用不存在帖子的comment记录数量: {}", orphanComments);
        totalInconsistencies += orphanComments;
        
        String sql11 = """
            SELECT COUNT(*) FROM comment c 
            WHERE c.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = c.user_id AND u.is_deleted = 0
            )
            """;
        Integer commentsWithoutUser = jdbcTemplate.queryForObject(sql11, Integer.class);
        log.info("引用不存在用户的comment记录数量: {}", commentsWithoutUser);
        totalInconsistencies += commentsWithoutUser;
        
        // 7. 检查book表的一致性
        String sql12 = """
            SELECT COUNT(*) FROM book b 
            WHERE b.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM trip t 
                WHERE t.id = b.trip_id AND t.is_deleted = 0
            )
            """;
        Integer orphanBooks = jdbcTemplate.queryForObject(sql12, Integer.class);
        log.info("引用不存在行程的book记录数量: {}", orphanBooks);
        totalInconsistencies += orphanBooks;
        
        // 8. 检查book_user表的一致性
        String sql13 = """
            SELECT COUNT(*) FROM book_user bu 
            WHERE bu.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM book b 
                WHERE b.id = bu.book_id AND b.is_deleted = 0
            )
            """;
        Integer orphanBookUsers = jdbcTemplate.queryForObject(sql13, Integer.class);
        log.info("引用不存在账本的book_user记录数量: {}", orphanBookUsers);
        totalInconsistencies += orphanBookUsers;
        
        String sql14 = """
            SELECT COUNT(*) FROM book_user bu 
            WHERE bu.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = bu.user_id AND u.is_deleted = 0
            )
            """;
        Integer bookUsersWithoutUser = jdbcTemplate.queryForObject(sql14, Integer.class);
        log.info("引用不存在用户的book_user记录数量: {}", bookUsersWithoutUser);
        totalInconsistencies += bookUsersWithoutUser;
        
        // 9. 检查account_book_record表的一致性
        String sql15 = """
            SELECT COUNT(*) FROM account_book_record abr 
            WHERE abr.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM book b 
                WHERE b.id = abr.book_id AND b.is_deleted = 0
            )
            """;
        Integer orphanRecords = jdbcTemplate.queryForObject(sql15, Integer.class);
        log.info("引用不存在账本的account_book_record记录数量: {}", orphanRecords);
        totalInconsistencies += orphanRecords;
        
        String sql16 = """
            SELECT COUNT(*) FROM account_book_record abr 
            WHERE abr.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = abr.user_id AND u.is_deleted = 0
            )
            """;
        Integer recordsWithoutUser = jdbcTemplate.queryForObject(sql16, Integer.class);
        log.info("引用不存在用户的account_book_record记录数量: {}", recordsWithoutUser);
        totalInconsistencies += recordsWithoutUser;
        
        String sql17 = """
            SELECT COUNT(*) FROM account_book_record abr 
            WHERE abr.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM account_book_category abc 
                WHERE abc.id = abr.category_id AND abc.is_deleted = 0
            )
            """;
        Integer recordsWithoutCategory = jdbcTemplate.queryForObject(sql17, Integer.class);
        log.info("引用不存在分类的account_book_record记录数量: {}", recordsWithoutCategory);
        totalInconsistencies += recordsWithoutCategory;
        
        // 10. 检查graph_info表的一致性
        String sql18 = """
            SELECT COUNT(*) FROM graph_info gi 
            WHERE gi.is_deleted = 0 AND gi.item_type = 1 
            AND NOT EXISTS (
                SELECT 1 FROM user u 
                WHERE u.id = gi.item_id AND u.is_deleted = 0
            )
            """;
        Integer orphanUserImages = jdbcTemplate.queryForObject(sql18, Integer.class);
        log.info("引用不存在用户的用户头像图片数量: {}", orphanUserImages);
        totalInconsistencies += orphanUserImages;
        
        String sql19 = """
            SELECT COUNT(*) FROM graph_info gi 
            WHERE gi.is_deleted = 0 AND gi.item_type = 2 
            AND NOT EXISTS (
                SELECT 1 FROM trip t 
                WHERE t.id = gi.item_id AND t.is_deleted = 0
            )
            """;
        Integer orphanTripImages = jdbcTemplate.queryForObject(sql19, Integer.class);
        log.info("引用不存在行程的行程图片数量: {}", orphanTripImages);
        totalInconsistencies += orphanTripImages;
        
        String sql20 = """
            SELECT COUNT(*) FROM graph_info gi 
            WHERE gi.is_deleted = 0 AND gi.item_type = 3 
            AND NOT EXISTS (
                SELECT 1 FROM place p 
                WHERE p.id = gi.item_id AND p.is_deleted = 0
            )
            """;
        Integer orphanPlaceImages = jdbcTemplate.queryForObject(sql20, Integer.class);
        log.info("引用不存在地点的地点图片数量: {}", orphanPlaceImages);
        totalInconsistencies += orphanPlaceImages;
        
        // 11. 检查每个行程是否有对应的账本（业务规则检查）
        String sql21 = """
            SELECT COUNT(*) FROM trip t 
            WHERE t.is_deleted = 0 
            AND NOT EXISTS (
                SELECT 1 FROM book b 
                WHERE b.trip_id = t.id AND b.is_deleted = 0
            )
            """;
        Integer tripsWithoutBook = jdbcTemplate.queryForObject(sql21, Integer.class);
        log.info("没有对应账本的行程数量: {}", tripsWithoutBook);
        // 注意：这个不算数据不一致，只是业务规则检查
        
        // 总结
        log.info("=== 数据一致性检查完成 ===");
        log.info("发现的数据不一致问题总数: {}", totalInconsistencies);
        
        if (totalInconsistencies == 0) {
            log.info("✅ 数据库数据完全一致，没有发现问题！");
        } else {
            log.warn("❌ 发现 {} 个数据不一致问题，建议运行清理方法", totalInconsistencies);
        }
        
        // 额外的业务规则检查
        log.info("=== 业务规则检查 ===");
        log.info("没有对应账本的行程数量: {} (这些行程可能需要创建账本)", tripsWithoutBook);
    }

    /**
     * 获取清理统计信息
     */
    @Test
    public void getCleanupStatistics() {
        log.info("获取清理统计信息...");
        
        // 统计各表的记录数量
        String[] tables = {
            "trip", "trip_user", "trip_place", "post", "post_like", 
            "comment", "invitation", "graph_info", "book", "book_user", 
            "account_book_record", "user", "place"
        };
        
        for (String table : tables) {
            try {
                String sql = "SELECT COUNT(*) FROM " + table + " WHERE is_deleted = 0";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
                log.info("表 {} 有效记录数量: {}", table, count);
            } catch (Exception e) {
                log.warn("统计表 {} 时出错: {}", table, e.getMessage());
            }
        }
    }
}