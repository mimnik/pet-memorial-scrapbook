# 宠物数字纪念册系统 - 工程文档

本文档为《宠物数字纪念册系统》的完整工程说明，包含了从底层架构到顶层业务的所有核心要素，旨在帮助开发人员和 AI 助手快速建立对该项目的准确理解。

## 1. 技术栈总览

- **后端**
  - **框架及版本**：Spring Boot 3.3.5
  - **JDK版本**：Java 21
  - **构建工具**：Maven 3.x
  - **安全框架**：Spring Security + JWT (jjwt 0.12.6)
  - **ORM框架**：Spring Data JPA (Hibernate)
  - **数据库**：MySQL 8.4.0（**注：项目已全面弃用H2数据库，统一使用 MySQL**）

- **前端**
  - **框架及版本**：Vue 3.5.27 (Composition API + `<script setup>`)
  - **UI组件库**：Element Plus 2.13.4 + Ant Design Vue 4.2.6 (混用，以 Element Plus 为主)
  - **状态管理**：Pinia 3.0.4
  - **路由**：Vue Router 5.0.3
  - **HTTP库**：Axios 1.13.6
  - **构建工具**：Vite 7.3.1 + TypeScript 5.9.3

- **部署方式**
  - Docker Compose + Nginx 反向代理 (包含 `backend.Dockerfile`, `frontend.Dockerfile`, `docker-compose.yml` 及 `nginx.conf`)。

## 2. 项目目录结构

```text
PetMemorialScrapbook/
├── backend/             # 后端 Spring Boot 源码目录
│   ├── src/main/java/   # 后端 Java 核心业务代码
│   ├── src/main/resources/ # 后端配置 (application.yml 等)
│   ├── pom.xml          # Maven 依赖及构建配置
├── frontend/            # 前端 Vue 3 + Vite 源码目录
│   ├── src/api/         # 集中请求管理的 Axios API 封装
│   ├── src/views/       # 页面级 Vue 视图组件
│   ├── src/router/      # 路由配置编排
│   ├── src/stores/      # Pinia 状态管理
│   ├── package.json     # Node.js 依赖及 NPM 脚本
├── database/            # 数据库初始化及演示 SQL 脚本 (schema/seed/init)
├── deploy/              # 容器化部署文件 (Dockerfile, docker-compose.yml, nginx.conf)
├── docs/                # 项目设计与业务文档（系统设计、UML 图等）
└── scripts/             # 本地开发一键启停脚本 (start-dev.bat / stop-dev.ps1)
```

## 3. 核心数据模型

所有模型严格对齐 MySQL 数据库表设计：

1. **`users` (用户表)**：核心字段 `username`, `password`, `role` (ROLE_USER / ROLE_ADMIN / ROLE_GUEST)。与 pets 为一对多关联。
2. **`pets` (宠物主档案)**：核心字段 `name`, `species`, `birth_date`, `is_public`, `owner_username`。
3. **`memory_entries` (回忆记录)**：核心字段 `title`, `content`, `image_url`, `video_url`。隶属 `pet_id`。
4. **`pet_archive_records` (档案与提醒)**：核心字段 `archive_type`, `reminder_at`, `reminder_status`。实现定时健康提醒及待办机制。
5. **`community_posts` (社区帖子 - 包含四个创新点)**：
   - **创新点对应字段 1**：`narrative_mode` (叙事模式：DAILY/MEMORIAL)
   - **创新点对应字段 2**：`mood_tag` (情绪标签：SUNNY/RAINY等)
   - **创新点对应字段 3**：`pet_voice` (布尔值，是否采用宠物第一视角)
   - **创新点对应字段 4**：`relay_enabled` (布尔值，是否开启纪念接力挑战)
6. **`community_topics` / `community_comments` / `community_post_likes`**：提供社区话题分类、关联回复和点赞功能。
7. **`user_follows` / `user_messages`**：社交体系数据实体，处理用户间关注与私信通讯。
8. **`content_reports` / `user_account_appeals`**：后台治理专属实体，处理违规内容举报与账号解封申诉。

## 4. 核心API接口清单 (摘要)

*基础路径均为 `/api`*

| 方法 | URL | 所属模块 | 说明 | 请求参数/响应 | 认证要求 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **POST** | `/auth/login` | 认证 | 用户账号密码登录 | 账号+密码 / 返回 JWT Token | 免认证 |
| **POST** | `/auth/guest` | 认证 | 游客快速登录 | 无 / 返回 游客 Token | 免认证 |
| **GET** | `/auth/me` | 认证 | 获取当前会话信息 | Bearer Token / 完整身份信息 | 需认证 |
| **GET/POST** | `/pets` | 档案 | 增改查宠物信息 | 宠物DTO / 包含分页的列表或实体 | 需认证 |
| **GET** | `/community/feed`| 社区 | 综合社区公开信息流 | 分页参数 / 聚合帖子流 | 游客及用户均可 |
| **POST** | `/community/posts`| 社区 | 创建新帖子/接力 | 创新点字段装载DTO / 创建结果 | 需认证 (限USER/ADMIN) |
| **PUT** | `/admin/users/{id}/status`| 治理 | 修改用户风控状态 | 封禁参数 / 成功标识 | 限 ROLE_ADMIN |

## 5. 功能模块与实现位置

1. **用户登录与认证** 
   - 前端：`frontend/src/views/UserLogin.vue`，拦截器位于 `src/utils/request.ts`。
   - 后端：`AuthController`，提供登入、注册、游客令牌签发。
2. **公开主页与宠物主页**
   - 前端：`frontend/src/views/HomePage.vue`, `PetDetailPage.vue`, `PublicHomePage.vue`。
3. **公开社交与社区大厅**
   - 前端：`frontend/src/views/CommunityPage.vue`。
   - 后端：`CommunityController` 负责处理信息流、热搜提取、关注流鉴权拉取。
4. **管理员风控中心** 
   - 前端：`frontend/src/views/AdminCenterPage.vue`（集成用户和社区管控、举报审理，以及右上角管理员弹窗菜单）。
   - 后端：`AdminController`, `ReportController` 聚合复杂的数据看板与管理流。

## 6. 创新功能实现要点

1. **双时态叙事 (`narrativeMode`)**
   - **采集**：发帖表单中设有时态切换（日常记录 vs 缅怀追忆）。
   - **展示**：前端渲染信息流时，依据此时态动态附加不同的 UI 蒙版和文案占位符，以区分“进行时”和“过去时”。
2. **情绪天气标签 (`moodTag`)**
   - **采集**：发帖时选择情绪状态（绑定预设的颜色及文案）。
   - **展示**：列表视图或详情页以胶囊标签和情绪色板高亮显示。
3. **宠物第一视角 (`petVoice`)**
   - **采集**：发布选项中的 Switch 开关。
   - **展示**：帖子列表中，原有的“发布人”头像和昵称，在开启此选项后将被替换为“宠物头像”及模拟发音格式展示。
4. **纪念接力挑战 (`relayEnabled`)**
   - **采集**：同样通过表单标记此贴是否作为“火种”。
   - **处理**：生成可关联追踪的链链式帖子，促进社区用户基于同一话题情感共鸣。

## 7. 配置文件与环境切换

- **主配置文件**：`backend/src/main/resources/application.yml`
- **环境变量注入**：
  - 数据源由环境变量 `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` 动态决定，本地默认指向 `localhost:3306/pet_memorial_db`。
  - 文件存储由 `${UPLOAD_DIR}` 限定（默认 `uploads/`）。
- **关键限制与安全**：
  - 允许大文件（视频）：`spring.servlet.multipart.max-file-size=1024MB`。
  - JWT 加密配置：`${JWT_SECRET}`（长度至少 32 字符），有效期 `${JWT_EXPIRATION}`（默认 24 小时）。

## 8. 页面路由结构 (Vue Router)

- **核心鉴权守卫**：
  - `/` (首页)：属于私有操作总览台 -> `requiresAuth: true, allowGuest: false`
  - `/login`：常规登录页 -> 凭证验证后重定向
  - `/community`：开放式社区 -> `requiresAuth: true, allowGuest: true` (游客也能进，但在触发写操作时被 API 拦截)
  - `/admin`：管理员中枢 -> `requiresAdmin: true`，仅允许鉴权角色为 ROLE_ADMIN 进入，越权访问将回退。

## 9. 状态管理 (Pinia Store)

- **模块名**：`user` (位于 `src/stores/user.ts`)
- **包含状态**：`token` (持久化至 localStorage)、`profile` (用户的脱敏身份载荷: username, role, avatar 等)。
- **暴漏方法**：`login()`, `guestLogin()`, `register()`, `refreshProfile()`, `logout()`。处理跨组件全局身份感知并与 API 双向绑定。

## 10. 功能完成情况

- **已完成功能清单**
  - 全量后端实体设计与 MySQL 方言适配
  - 核心业务骨架（档案增改、社区信息流、关注机制）
  - 基于 Spring Security 的完善的三端（ADMIN/USER/GUEST）访问隔离
  - 前面所述的四个创新级功能点字段就绪
  - 前端各级页面视图（包含最新的 Admin头像状态管理改进）
- **待开发/规划功能清单**
  - 极个别异常交互（如极速大量请求拦截）进一步压测调优。
  - 文件彻底脱离本地路径向分布式云存储转移（可选重构）。

## 11. 构建与运行命令

- **后端**
  - 打包：`./mvnw clean package`
  - 开发热运行：`./mvnw spring-boot:run`
- **前端**
  - 安装依赖及环境：`v22.x Node.js` 及其配套 `npm`
  - 开发服务：`npm run dev` (通过 vite 提供 HMR)
  - 类型检查：`npm run type-check`
  - 生产构建打包：`npm run build-only`
- **快捷起停**
  - 利用项目根部提供的 `scripts/start-dev.bat` 或 `stop-dev.ps1` 一键拉起或终止本地环境。
- **容器全套部署**
  - 需位于 `deploy/` 下执行 `docker-compose up -d --build` 进行统一代理挂载。

## 12. 异常处理与安全机制

- **全局参数验证与异常拦截**：后端包含 `@RestControllerAdvice` ，任何诸如外键遗失、字符串超长、枚举不匹配皆统装配为 `code/message/data` 规范 400 警告反馈。
- **安全阻断**：所有访问 /api/ 保护资源的请求首先过 JWT Filter（不匹配/过期=401）；即使 JWT 有效，若尝试操作他人资源或越权管理接口，Service 层校验截断为 403 。
- **密码存储**：使用 `BCryptPasswordEncoder` 保存强哈希值，确保不可逆。
