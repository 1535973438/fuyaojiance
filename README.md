# 服药监测系统

一个用于管理服药计划、记录服药情况、追踪检查结果的移动端应用系统。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.1
- **语言**: Java 17
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0+
- **构建工具**: Maven
- **其他**: Lombok, Jackson

### 前端
- **框架**: Vue 3.4.15
- **构建工具**: Vite 5.0.11
- **UI组件库**: Vant 4.8.4
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **HTTP客户端**: Axios 1.6.5
- **图表库**: ECharts 5.4.3
- **日期处理**: Day.js 1.11.10

## 主要功能

### 1. 药品管理
- 药品信息的增删改查
- 库存管理
- 规格管理

### 2. 服药计划管理
- 创建服药计划（支持每日、间隔、每周指定天数）
- 设置服药时间和有效期
- 一个计划可关联多个药品
- 计划启用/停用

### 3. 服药记录
- 查看指定日期的服药记录
- 服药打卡（已服）
- 撤销服药（误操作可撤销，恢复库存）
- 补服功能（支持填写服用时间）
- 定时任务每天凌晨1点自动生成当日记录

### 4. 检查记录管理
- 检查项的预设和管理
- 检查记录的增删改查
- 检查报告照片上传
- 异常值标记（超出参考范围）

### 5. 统计分析
- 服药完成率统计（按周/月/全部）
- 检查趋势图表展示
- 最近检查结果展示
- 支持多时间段筛选

### 6. 用户认证
- 简单登录认证（账号密码配置在配置文件中）
- Token认证（48小时有效期，活跃用户自动续期）
- 记住密码功能（一周）

## 环境要求

### 后端环境
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+ 数据库

### 前端环境
- Node.js 16+ 
- npm 或 yarn

## 快速开始

### 1. 数据库准备

创建数据库并执行SQL脚本：
```sql
CREATE DATABASE medication_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入数据库结构：
```bash
mysql -u root -p medication_tracker < backend/src/main/resources/schema.sql
```

### 2. 后端配置

修改数据库配置 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medication_tracker?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

# 登录配置
auth:
  username: admin
  password: 123456

# 文件上传配置（根据实际情况修改）
file:
  upload-path: /data/uploads/
```

启动后端服务：
导入idea后，自动加载maven脚本，直接运行即可

后端服务默认运行在：http://localhost:8080

### 3. 前端配置

安装依赖：
```bash
cd frontend
npm install
```

启动开发服务器：
```bash
npm run dev
```

前端服务默认运行在：http://localhost:5173

### 4. 生产构建

前端构建：
```bash
cd frontend
npm run build
```

构建产物在 `frontend/dist/` 目录

## 默认账号

- 用户名: `admin`
- 密码: `123456`

（可在 `backend/src/main/resources/application.yml` 中修改）

