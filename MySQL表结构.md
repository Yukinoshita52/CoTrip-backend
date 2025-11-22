# MySQL表结构

## 建表语句

1. 行程信息表

   ```mysql
   CREATE TABLE trip (
       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行程ID',
       name VARCHAR(20) NOT NULL COMMENT '行程名称',
       region VARCHAR(20) NOT NULL COMMENT '城市',
       start_date DATE NOT NULL COMMENT '开始时间',
       end_date DATE NOT NULL COMMENT '结束时间',
       description VARCHAR(255) DEFAULT NULL COMMENT '行程描述',
   
       created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';
   ```

2. 地点信息表

   ```mysql
   CREATE TABLE place (
       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地点ID',
       name VARCHAR(100) NOT NULL COMMENT '地点名称',
       type_id TINYINT NOT NULL COMMENT '地点类型ID，关联 place_type(id)',
       uid VARCHAR(64) NOT NULL COMMENT 'POI唯一标识',
       lat FLOAT NOT NULL COMMENT '纬度',
   	lng FLOAT NOT NULL COMMENT '经度',
       address VARCHAR(255) DEFAULT NULL COMMENT 'POI所在地址',
       telephone VARCHAR(20) DEFAULT NULL COMMENT 'POI电话',
       detail_info JSON DEFAULT NULL COMMENT '详细信息',
   
       created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
   
       UNIQUE KEY uniq_uid (uid),
       INDEX idx_type_id (type_id),
       INDEX idx_lat_lng (lat, lng),
       INDEX idx_name (name)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点信息表';

3. 地点类型表

   ```mysql
   CREATE TABLE place_type (
       id TINYINT PRIMARY KEY COMMENT '类型ID',
       code VARCHAR(20) UNIQUE NOT NULL COMMENT '类型编码（英文，如 sight/hotel/restaurant/transport）',
       name VARCHAR(50) NOT NULL COMMENT '类型名称（中文显示）',
       
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点类型表';
   
   ```

   

4. 行程-地点关系表

   ```mysql
   CREATE TABLE trip_place (
       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系表ID',
       trip_id BIGINT NOT NULL COMMENT '行程ID',
       place_id BIGINT NOT NULL COMMENT '地点ID',
       day INT NOT NULL COMMENT '第几天',
   
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程-地点关系表';
   
   ```

   

5. 帖子表（post）

   ```mysql
   CREATE TABLE post (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       trip_id BIGINT NOT NULL COMMENT '行程ID',
   
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';
   
   ```

   

6. 邀请信息表

   ```mysql
   CREATE TABLE invitation (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       invitee VARCHAR(11) NOT NULL COMMENT '被邀请人手机号',
       status TINYINT NOT NULL DEFAULT 0 COMMENT '邀请状态：0-待接受，1-已接受，2-已拒绝，3-已过期',
   
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请信息表';
   
   ```

   

7. 用户信息表

   ```mysql
   CREATE TABLE user (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名',
       password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
       nickname VARCHAR(16) DEFAULT NULL COMMENT '用户昵称',
       avatar_id BIGINT DEFAULT NULL COMMENT '头像ID',
       phone VARCHAR(11) UNIQUE COMMENT '手机号',
   
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
   
   ```

   

8. 图片信息表

   ```mysql
   CREATE TABLE graph_info (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(128) NOT NULL COMMENT '图片名称',
       item_type TINYINT NOT NULL COMMENT '图片所属对象类型：1-用户头像，2-行程图片，3-地点图片，4-其他',
       item_id BIGINT NOT NULL COMMENT '图片所属对象ID',
       url VARCHAR(255) NOT NULL COMMENT '图片地址',
   
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片信息表';
   
   ```

9. 评论表

   ```sql
   CREATE TABLE comment (
       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
       post_id BIGINT NOT NULL COMMENT '帖子ID，对应 post.id',
       user_id BIGINT NOT NULL COMMENT '评论用户ID，对应 user.id',
       content VARCHAR(500) NOT NULL COMMENT '评论内容',
       parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，用于楼中楼',
       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
       -- INDEX idx_post_id (post_id),
       -- INDEX idx_user_id (user_id),
       -- INDEX idx_parent_id (parent_id)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
   
   ```

   

10. 点赞表

    ```sql
    CREATE TABLE post_like (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
        post_id BIGINT NOT NULL COMMENT '帖子ID，对应 post.id',
        user_id BIGINT NOT NULL COMMENT '用户ID，对应 user.id',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
        is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
        -- UNIQUE KEY uniq_post_user (post_id, user_id),
        -- INDEX idx_post_id (post_id),
        -- INDEX idx_user_id (user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';
    
    ```

    

11. 记账分类表

    ```sql
    CREATE TABLE account_book_category (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
        user_id BIGINT NOT NULL COMMENT '所属用户ID',
        name VARCHAR(100) NOT NULL COMMENT '分类名称',
        type TINYINT NOT NULL COMMENT '类型：1-支出 2-收入',
        icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
        sort INT DEFAULT 0 COMMENT '排序值',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
        -- INDEX idx_user (user_id),
        -- INDEX idx_type (type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账分类表';
    
    ```

    

12. 记账流水表

    ```sql
    CREATE TABLE account_book_record (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        category_id BIGINT NOT NULL COMMENT '分类ID',
        amount DECIMAL(10,2) NOT NULL COMMENT '金额',
        type TINYINT NOT NULL COMMENT '类型：1-支出 2-收入',
        record_time DATE NOT NULL COMMENT '记账日期',
        remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
        -- INDEX idx_user (user_id),
        -- INDEX idx_category (category_id),
        -- INDEX idx_record_time (record_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账流水表';
    
    ```

    

13. 行程-用户关系表

    ```sql
    CREATE TABLE trip_user (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行程-用户关系表ID',
        trip_id BIGINT NOT NULL COMMENT '行程ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-创建者 1-参与者',
    
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
        -- PRIMARY KEY (`id`),
      	-- UNIQUE KEY `uk_trip_user` (`trip_id`,`user_id`) COMMENT '同一用户不能重复加入行程'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程-用户关系表';
    
    ```

    

14. 公告表

    ```sql
    CREATE TABLE `announcement` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
      `content` text COLLATE utf8mb4_general_ci NOT NULL,
      `publish_time` datetime DEFAULT NULL,
      `author_id` bigint DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
    ```

    

15. 

    

    