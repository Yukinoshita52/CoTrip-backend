# CoTrip Backend

多人协同旅行规划系统后端服务 - 基于Spring Boot的RESTful API

## 📋 项目简介

CoTrip（Collaborative Trip）是一个多人协同旅行规划系统，支持行程规划、费用记账、社区分享等功能。本项目为后端服务，提供完整的API接口。

## 🏗️ 技术架构

### 核心技术栈
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **文件存储**: MinIO
- **ORM**: MyBatis Plus
- **文档**: SpringDoc (Swagger)
- **构建工具**: Maven

### 项目结构
```
CoTrip-backend/
├── common/          # 公共模块
│   ├── src/main/java/com/trip/common/
│   │   ├── login/   # 登录相关
│   │   └── result/  # 统一返回结果
│   └── pom.xml
├── model/           # 数据模型模块
│   ├── src/main/java/com/trip/model/
│   │   ├── entity/  # 实体类
│   │   ├── dto/     # 数据传输对象
│   │   └── vo/      # 视图对象
│   └── pom.xml
├── web/             # Web服务模块
│   ├── src/main/java/com/trip/web/
│   │   ├── controller/  # 控制器
│   │   ├── service/     # 业务逻辑
│   │   ├── mapper/      # 数据访问
│   │   └── config/      # 配置类
│   ├── src/main/resources/
│   │   ├── application*.yml  # 配置文件
│   │   └── com/trip/mapper/  # MyBatis映射文件
│   └── pom.xml
└── pom.xml          # 父级POM
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- MinIO (对象存储)

### 本地开发

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd CoTrip-backend
   ```

2. **配置数据库**
   ```sql
   CREATE DATABASE `co-trip` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

3. **配置环境**
   - 复制 `application-dev.yml.example` 为 `application-dev.yml`
   - 修改数据库、Redis、MinIO连接配置

4. **启动服务**
   ```bash
   # 使用Maven
   mvn spring-boot:run -pl web
   
   # 或使用IDE
   # 运行 web/src/main/java/com/trip/web/WebApplication.java
   ```

5. **访问接口文档**
   ```
   http://localhost:8080/swagger-ui.html
   ```

### 生产部署

#### 方式1：Docker部署（推荐）

1. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Docker部署**
   ```bash
   # 复制jar包到部署目录
   cp web/target/web-*.jar /opt/cotrip-api/web.jar
   
   # 使用Docker Compose启动
   cd /opt/cotrip-api
   docker compose up -d --build
   ```

#### 方式2：直接部署

1. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

2. **启动服务**
   ```bash
   java -jar web/target/web-*.jar --spring.profiles.active=prod
   ```

## 📝 API 文档

### 主要模块

#### 🔐 认证模块 (`/api/auth`)
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册

#### 🗺️ 行程模块 (`/api/trips`)
- `GET /trips` - 获取行程列表
- `POST /trips` - 创建行程
- `GET /trips/{id}` - 获取行程详情
- `PUT /trips/{id}` - 更新行程
- `DELETE /trips/{id}` - 删除行程

#### 💰 记账模块 (`/api/account`)
- `GET /account/book/list` - 获取账本列表
- `POST /account/book` - 创建账本
- `GET /account/record/list` - 获取账单列表
- `POST /account/record` - 创建账单

#### 🌍 社区模块 (`/api/community`)
- `GET /community/feed` - 获取社区动态
- `POST /community/post` - 发布帖子
- `GET /community/post/{id}` - 获取帖子详情
- `POST /community/post/{id}/like` - 点赞帖子

#### 🖼️ 文件模块 (`/api/images`)
- `POST /images/upload` - 上传图片
- `GET /images/{id}` - 获取图片

### 接口规范

**统一返回格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**认证方式**:
```
Authorization: Bearer <token>
```

## ⚙️ 配置说明

### 环境配置

- `application.yml` - 主配置文件
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

### 关键配置项

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/co-trip
    username: your-username
    password: your-password

# Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password

# MinIO配置
minio:
  endpoint: http://localhost:9000
  accessKey: your-access-key
  secretKey: your-secret-key
  bucketName: cotrip
```

## 🔧 开发指南

### 代码规范
- 使用统一的代码格式化配置
- 遵循RESTful API设计规范
- 使用统一的异常处理机制
- 添加适当的日志记录

### 数据库设计
- 使用逻辑删除（is_deleted字段）
- 统一的时间字段（create_time, update_time）
- 合理的索引设计

### 缓存策略
- 使用Redis缓存热点数据
- 实现缓存失效机制
- 合理设置缓存过期时间

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 跳过测试构建
mvn clean package -DskipTests
```

## 📊 性能优化

### 已实现的优化
- Redis缓存加速接口响应
- 数据库连接池优化
- 分页查询优化
- 图片压缩和CDN加速

### 监控指标
- 接口响应时间
- 数据库连接数
- Redis内存使用
- 系统资源使用

## 🔒 安全特性

- JWT Token认证
- 接口权限控制
- SQL注入防护
- XSS攻击防护
- 文件上传安全检查

## 📈 版本历史

### v1.0.0
- 基础功能实现
- 用户认证系统
- 行程管理功能
- 记账功能
- 社区分享功能

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: your-email@example.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！
