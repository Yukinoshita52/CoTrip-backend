# MySQL表结构

## 实体表（Entity Tables）

### 行程表(trip)

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

### 地点表(place)

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
```



### 帖子表(post)

```mysql
CREATE TABLE post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL COMMENT '行程ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

```

### 邀请表(invitation)

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

### 用户表(user)

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

### 图片表(graph_info)

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

### 评论表(comment)

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

```

### 点赞表(post_like)

```sql
CREATE TABLE post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID，对应 post.id',
    user_id BIGINT NOT NULL COMMENT '用户ID，对应 user.id',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

```

### 账本表(book)

> 注：一个行程（trip）只能对应一个账本（book），一个账本（book）由多个用户（user）共享

```sql
CREATE TABLE `book` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账本表ID',
    trip_id BIGINT NOT NULL COMMENT '所属行程ID',
    name VARCHAR(30) NOT NULL COMMENT '账本名称',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本表';

```



### 记账分类表(account_book_category)

```sql
CREATE TABLE account_book_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    type TINYINT NOT NULL COMMENT '类型：1-支出 2-收入',
    icon_id BIGINT DEFAULT NULL COMMENT '图标url(对应graph_info.id)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账分类表';

```

### 记账流水表(account_book_record)

```sql
CREATE TABLE account_book_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
    book_id BIGINT NOT NULL COMMENT '所属账本ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    type TINYINT NOT NULL COMMENT '类型：1-支出 2-收入',
    record_time DATE NOT NULL COMMENT '记账日期',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账流水表';

```

### 公告表(announcement)

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





## 关系表（Relationship Table）

### 地点-类型关系表(place_type)

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

### 行程-地点关系表(trip_place)

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

### 行程-用户关系表(trip_user)

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

### 账本-用户关系表(book_user)

```sql
CREATE TABLE book_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账本-用户关系表ID',
    book_id BIGINT NOT NULL COMMENT '账本ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本-用户关系表';

```



