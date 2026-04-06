# 4 系统设计

本章在需求分析基础上，完成宠物数字纪念册系统的整体架构、功能模块、数据库与接口设计。设计过程围绕情感化纪念、社区互动、创新功能落地三大核心目标，采用成熟稳定的技术方案，确保系统可开发、可扩展、可维护。

## 4.1 系统总体架构设计

### 4.1.1 架构设计目标

系统总体架构遵循以下设计目标：

1. 可实现性：采用前后端分离的 B/S 架构，降低实现复杂度，适配毕业设计周期。
2. 可扩展性：业务按领域模块拆分，便于后续新增“纪念提醒策略”“推荐算法参数配置”等功能。
3. 可维护性：后端采用 controller-service-repository 分层，前端采用 views-api-stores-types 分层，接口契约统一。
4. 可部署性：同时支持本地开发（H2/脚本一键启动）与容器化部署（Docker Compose + Nginx 反向代理）。
5. 安全性：采用 JWT 无状态鉴权与角色授权，保证用户私有数据、后台治理能力不被越权访问。

### 4.1.2 总体架构分层

系统采用四层逻辑架构：

1. 表现层（Frontend）

- 技术：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router。
- 职责：页面渲染、交互编排、路由守卫、状态管理、上传预处理（视频时长/封面）。

2. 接口与业务层（Backend API + Service）

- 技术：Spring Boot 3.3.5 + Spring Security + JWT + JPA。
- 职责：鉴权授权、业务规则执行、数据校验、统一响应与异常处理。

3. 数据持久层（Repository + Database）

- 技术：Spring Data JPA + H2/MySQL。
- 职责：实体映射、关系约束、索引优化、事务一致性。

4. 文件存储层（Upload Storage）

- 形式：后端本地文件系统目录，上传根路径由 `app.upload-dir`（环境变量 `UPLOAD_DIR`）配置，默认 `uploads`，当前容器部署使用 `/home/PetDatabase`。
- 职责：图片/视频文件落盘，并通过 `/uploads/**` 静态资源映射对外分发访问路径。

### 4.1.3 技术选型与选型理由

| 层级     | 技术方案               | 选型理由                            |
| -------- | ---------------------- | ----------------------------------- |
| 前端框架 | Vue 3 + TS             | 组件化与类型约束兼具，开发效率高    |
| UI 组件  | Element Plus           | 表单、表格、弹窗等后台型场景成熟    |
| 状态管理 | Pinia                  | 轻量易维护，适合用户会话与全局状态  |
| 路由     | Vue Router             | 支持鉴权守卫与角色分流              |
| 后端框架 | Spring Boot            | 生态完善，适合 REST 服务快速落地    |
| 安全     | Spring Security + JWT  | 无状态鉴权，便于前后端分离部署      |
| ORM      | Spring Data JPA        | 降低 CRUD 成本，快速构建业务模型    |
| 数据库   | H2 / MySQL             | H2 便于开发演示，MySQL 便于正式部署 |
| 部署     | Docker Compose + Nginx | 一键多服务编排，前后端代理清晰      |

### 4.1.4 运行与部署架构

系统支持两种运行模式：

1. 本地开发模式

- 前端 Vite 服务运行于 3000。
- 后端 Spring Boot 服务运行于 8080。
- 默认使用 H2 文件数据库，便于快速启动和调试。

2. 容器部署模式

- 前端容器内置 Nginx，对外提供 3000（容器内 80）访问。
- /api 与 /uploads 请求由 Nginx 反向代理到后端。
- 后端容器连接 MySQL 8.4，上传目录通过宿主机目录绑定持久化（`/home/PetDatabase:/home/PetDatabase`）。

### 4.1.5 安全与权限架构

1. 认证

- 登录/注册/游客登录后签发 JWT。
- 前端请求拦截器自动注入 Bearer Token。

2. 授权

- 角色包括 ROLE_USER、ROLE_ADMIN、ROLE_GUEST。
- 管理接口 /api/admin/** 仅管理员可访问。
- 游客可访问社区只读能力（`/api/community/feed`、`/api/community/recommendations`、`/api/community/hot-pets`、`/api/community/topics`、`/api/community/posts/{postId}/comments`）。
- 关注流（`/api/community/following-feed`）及社区写操作仅对 ROLE_USER/ROLE_ADMIN 开放。

3. 统一异常

- 全局异常处理器统一输出错误码与错误信息。
- 参数校验失败返回 400，权限不足返回 403，认证失败返回 401。

## 4.2 系统功能模块设计

### 4.2.1 模块划分总览

系统按业务域划分为 8 个核心模块：

| 模块               | 核心能力                              | 主要参与者           |
| ------------------ | ------------------------------------- | -------------------- |
| 认证与会话模块     | 注册、登录、游客登录、当前身份获取    | 普通用户/游客/管理员 |
| 宠物资料模块       | 宠物档案 CRUD、公开状态管理、详情编辑 | 普通用户             |
| 回忆时间线模块     | 回忆记录 CRUD、图文视频沉淀           | 普通用户             |
| 宠物档案与提醒模块 | 健康档案记录、到期提醒、稍后处理      | 普通用户             |
| 社区互动模块       | 发帖、点赞、评论、推荐、热榜、关注流  | 普通用户/游客        |
| 社交通信模块       | 关注关系、私信会话与已读状态          | 普通用户             |
| 治理与风控模块     | 举报、申诉、用户状态治理、内容审核    | 管理员/普通用户      |
| 公开访问模块       | Token 分享页、用户公开主页、用户搜索  | 普通用户/游客        |

### 4.2.2 认证与会话模块

1. 功能点

- 用户注册与登录。
- 游客快速登录（低门槛体验入口）。
- 当前会话身份回读（用于页面权限控制）。

2. 设计要点

- 会话采用 JWT，无服务端 Session 粘连。
- 前端 Pinia + localStorage 存储 token 与当前用户信息。
- 路由守卫按角色自动分流：管理员优先进入管理中心，游客限制写操作页面。

### 4.2.3 宠物资料与回忆模块

1. 功能点

- 宠物基础信息维护（昵称、物种、品种、性别、生日、纪念日、体重等）。
- 宠物详情展示与字段点击编辑。
- 回忆时间线管理（标题、内容、日期、地点、图片、视频）。

2. 设计要点

- 宠物与回忆采用一对多关系。
- 年龄由生日自动计算，避免人工填写误差。
- 回忆内容支持图文与视频混合展示，增强纪念沉浸感。

### 4.2.4 宠物档案与提醒模块

1. 功能点

- 档案记录类型：疫苗、体检、驱虫、用药、其他。
- 档案提醒开关与提醒时间设置。
- 到期提醒处理：已完成、稍后提醒。

2. 设计要点

- 提醒状态采用 PENDING/COMPLETED 状态机。
- 稍后处理通过 delayHours 重新计算 reminderAt。
- 首页统一提醒弹窗提升任务闭环率。

### 4.2.5 社区互动与创新功能模块

1. 功能点

- 社区信息流筛选：叙事模式、情绪标签、关键词、话题。
- 发帖互动：点赞、评论、纪念接力。
- 推荐与热榜：推荐内容列表、宠物热度榜。

2. 创新设计

- 叙事模式（DAILY/MEMORIAL）强化内容表达维度。
- 情绪标签 + 宠物口吻 + 接力机制，增强情感化互动。
- 推荐与热榜将“记录”延展为“共创”，形成社区活性循环。

### 4.2.6 社交通信与治理模块

1. 社交通信

- 关注/取关。
- 关注与粉丝列表。
- 私信会话、已读状态、未读汇总。

2. 治理风控

- 用户举报内容并追踪个人举报单。
- 用户提交账号申诉（含冻结用户公共入口）。
- 管理员执行用户状态管理、内容审核、申诉处理、概览看板。

### 4.2.7 公开访问模块

1. 功能点

- Token 公开页：按 shareToken 访问单宠物公开内容。
- 用户公开主页：按 ownerUsername 聚合公开宠物、回忆与公开社区内容。
- 公开用户搜索：支持关键词与限制条数。

2. 设计价值

- 将“私密纪念”扩展到“可控公开分享”。
- 支持对外展示且不暴露私密数据，平衡传播与隐私。

## 4.3 数据库设计

### 4.3.1 数据库选型与设计原则

1. 选型

- 开发/演示环境：H2（文件模式）。
- 部署环境：MySQL 8.4。

2. 设计原则

- 结构清晰：按业务域拆分表。
- 约束优先：主键、唯一约束、外键与级联删除保障一致性。
- 可查询：对高频场景建立复合索引。
- 可扩展：保留可增长字段与状态字段，便于后续演进。

### 4.3.2 核心数据实体

以下按“字段名、类型、长度、是否为空、键、注释”格式给出核心实体表字段定义。

#### 1) users（用户主表）

| 字段名             | 类型     | 长度 | 是否为空 | 键       | 注释                                    |
| ------------------ | -------- | ---- | -------- | -------- | --------------------------------------- |
| id                 | BIGINT   | -    | 否       | PK, 自增 | 用户主键ID                              |
| username           | VARCHAR  | 100  | 否       | UK       | 用户名（唯一）                          |
| email              | VARCHAR  | 120  | 否       | UK       | 邮箱（唯一）                            |
| password           | VARCHAR  | 120  | 否       | -        | 密码哈希                                |
| display_name       | VARCHAR  | 100  | 是       | -        | 显示昵称                                |
| avatar_url         | VARCHAR  | 500  | 是       | -        | 头像URL                                 |
| bio                | VARCHAR  | 1000 | 是       | -        | 个人简介                                |
| role               | VARCHAR  | 20   | 否       | -        | 角色（ROLE_USER/ROLE_ADMIN/ROLE_GUEST） |
| account_frozen     | BIT      | -    | 否       | -        | 是否冻结账号（默认0）                   |
| posting_restricted | BIT      | -    | 否       | -        | 是否限制发布（默认0）                   |
| warning_count      | INT      | -    | 否       | -        | 警告次数（默认0）                       |
| admin_note         | VARCHAR  | 1000 | 是       | -        | 管理员备注                              |
| created_at         | DATETIME | -    | 否       | -        | 创建时间                                |
| updated_at         | DATETIME | -    | 否       | -        | 更新时间                                |

#### 2) pets（宠物主档案）

| 字段名             | 类型     | 长度 | 是否为空 | 键       | 注释                            |
| ------------------ | -------- | ---- | -------- | -------- | ------------------------------- |
| id                 | BIGINT   | -    | 否       | PK, 自增 | 宠物主键ID                      |
| name               | VARCHAR  | 100  | 否       | -        | 宠物名称                        |
| species            | VARCHAR  | 100  | 是       | -        | 物种                            |
| breed              | VARCHAR  | 100  | 是       | -        | 品种                            |
| gender             | VARCHAR  | 20   | 是       | -        | 性别                            |
| birth_date         | DATE     | -    | 是       | -        | 生日                            |
| memorial_date      | DATE     | -    | 是       | -        | 纪念日                          |
| age                | INT      | -    | 是       | -        | 年龄（可由生日自动计算）        |
| weight             | VARCHAR  | 20   | 是       | -        | 体重描述                        |
| marital_status     | VARCHAR  | 20   | 是       | -        | 婚姻状态                        |
| skills             | VARCHAR  | 500  | 是       | -        | 特长                            |
| dietary_habits     | VARCHAR  | 1000 | 是       | -        | 饮食习惯                        |
| physical_condition | VARCHAR  | 1000 | 是       | -        | 身体状况                        |
| avatar_url         | VARCHAR  | 500  | 是       | -        | 宠物头像URL                     |
| description        | VARCHAR  | 2000 | 是       | -        | 宠物简介                        |
| owner_username     | VARCHAR  | 100  | 否       | FK, IDX  | 所属用户（关联 users.username） |
| is_public          | BIT      | -    | 否       | -        | 是否公开（默认0）               |
| share_token        | VARCHAR  | 64   | 否       | UK       | 分享令牌（唯一）                |
| created_at         | DATETIME | -    | 否       | -        | 创建时间                        |
| updated_at         | DATETIME | -    | 否       | -        | 更新时间                        |

#### 3) memory_entries（回忆记录）

| 字段名                 | 类型     | 长度 | 是否为空 | 键            | 注释                       |
| ---------------------- | -------- | ---- | -------- | ------------- | -------------------------- |
| id                     | BIGINT   | -    | 否       | PK, 自增      | 回忆主键ID                 |
| pet_id                 | BIGINT   | -    | 否       | FK, IDX(复合) | 所属宠物ID（关联 pets.id） |
| title                  | VARCHAR  | 200  | 否       | -             | 回忆标题                   |
| content                | VARCHAR  | 5000 | 否       | -             | 回忆正文                   |
| event_date             | DATE     | -    | 是       | IDX(复合)     | 事件日期                   |
| location               | VARCHAR  | 200  | 是       | -             | 地点                       |
| image_url              | VARCHAR  | 500  | 是       | -             | 图片URL                    |
| video_url              | VARCHAR  | 500  | 是       | -             | 视频URL                    |
| video_cover_url        | VARCHAR  | 500  | 是       | -             | 视频封面URL                |
| video_duration_seconds | INT      | -    | 是       | -             | 视频时长（秒）             |
| created_at             | DATETIME | -    | 否       | -             | 创建时间                   |
| updated_at             | DATETIME | -    | 否       | -             | 更新时间                   |

#### 4) pet_archive_records（宠物档案与提醒）

| 字段名                | 类型     | 长度 | 是否为空 | 键            | 注释                       |
| --------------------- | -------- | ---- | -------- | ------------- | -------------------------- |
| id                    | BIGINT   | -    | 否       | PK, 自增      | 档案记录主键ID             |
| pet_id                | BIGINT   | -    | 否       | FK, IDX(复合) | 所属宠物ID（关联 pets.id） |
| archive_type          | VARCHAR  | 30   | 否       | -             | 档案类型                   |
| title                 | VARCHAR  | 120  | 否       | -             | 记录标题                   |
| metric_value          | VARCHAR  | 100  | 是       | -             | 指标值                     |
| unit                  | VARCHAR  | 20   | 是       | -             | 单位                       |
| event_date            | DATE     | -    | 是       | IDX(复合)     | 记录日期                   |
| note                  | VARCHAR  | 1000 | 是       | -             | 备注                       |
| reminder_enabled      | BIT      | -    | 否       | IDX(复合)     | 是否启用提醒（默认0）      |
| reminder_at           | DATETIME | -    | 是       | IDX(复合)     | 提醒时间                   |
| reminder_status       | VARCHAR  | 20   | 否       | IDX(复合)     | 提醒状态（默认PENDING）    |
| reminder_completed_at | DATETIME | -    | 是       | -             | 提醒完成时间               |
| created_at            | DATETIME | -    | 否       | -             | 创建时间                   |
| updated_at            | DATETIME | -    | 否       | -             | 更新时间                   |

#### 5) community_topics（社区话题）

| 字段名              | 类型     | 长度 | 是否为空 | 键       | 注释             |
| ------------------- | -------- | ---- | -------- | -------- | ---------------- |
| id                  | BIGINT   | -    | 否       | PK, 自增 | 话题主键ID       |
| name                | VARCHAR  | 50   | 否       | UK       | 话题名称（唯一） |
| description         | VARCHAR  | 500  | 是       | -        | 话题描述         |
| created_by_username | VARCHAR  | 100  | 否       | -        | 创建人用户名     |
| created_at          | DATETIME | -    | 否       | -        | 创建时间         |
| updated_at          | DATETIME | -    | 否       | -        | 更新时间         |

#### 6) community_posts（社区帖子）

| 字段名                 | 类型     | 长度 | 是否为空 | 键            | 注释                                   |
| ---------------------- | -------- | ---- | -------- | ------------- | -------------------------------------- |
| id                     | BIGINT   | -    | 否       | PK, 自增      | 帖子主键ID                             |
| pet_id                 | BIGINT   | -    | 否       | FK            | 关联宠物ID（关联 pets.id）             |
| topic_id               | BIGINT   | -    | 是       | FK, IDX(复合) | 关联话题ID（关联 community_topics.id） |
| author_username        | VARCHAR  | 100  | 否       | IDX(复合)     | 作者用户名                             |
| title                  | VARCHAR  | 200  | 否       | -             | 帖子标题                               |
| content                | VARCHAR  | 5000 | 否       | -             | 帖子正文                               |
| image_url              | VARCHAR  | 500  | 是       | -             | 图片URL                                |
| video_url              | VARCHAR  | 500  | 是       | -             | 视频URL                                |
| video_cover_url        | VARCHAR  | 500  | 是       | -             | 视频封面URL                            |
| video_duration_seconds | INT      | -    | 是       | -             | 视频时长（秒）                         |
| mood_tag               | VARCHAR  | 20   | 否       | -             | 情绪标签（默认SUNNY）                  |
| narrative_mode         | VARCHAR  | 20   | 否       | -             | 叙事模式（默认DAILY）                  |
| pet_voice              | BIT      | -    | 否       | -             | 是否宠物口吻（默认0）                  |
| relay_enabled          | BIT      | -    | 否       | -             | 是否接力（默认0）                      |
| like_count             | INT      | -    | 否       | -             | 点赞数（默认0）                        |
| comment_count          | INT      | -    | 否       | -             | 评论数（默认0）                        |
| hidden_by_admin        | BIT      | -    | 否       | -             | 是否被管理员屏蔽（默认0）              |
| created_at             | DATETIME | -    | 否       | IDX(复合)     | 创建时间（信息流排序）                 |
| updated_at             | DATETIME | -    | 否       | -             | 更新时间                               |

#### 7) community_comments（社区评论）

| 字段名          | 类型     | 长度 | 是否为空 | 键            | 注释                                  |
| --------------- | -------- | ---- | -------- | ------------- | ------------------------------------- |
| id              | BIGINT   | -    | 否       | PK, 自增      | 评论主键ID                            |
| post_id         | BIGINT   | -    | 否       | FK, IDX(复合) | 所属帖子ID（关联 community_posts.id） |
| author_username | VARCHAR  | 100  | 否       | -             | 评论作者用户名                        |
| content         | VARCHAR  | 1000 | 否       | -             | 评论内容                              |
| relay_reply     | BIT      | -    | 否       | -             | 是否接力回复（默认0）                 |
| hidden_by_admin | BIT      | -    | 否       | -             | 是否被管理员屏蔽（默认0）             |
| created_at      | DATETIME | -    | 否       | IDX(复合)     | 创建时间                              |
| updated_at      | DATETIME | -    | 否       | -             | 更新时间                              |

#### 8) community_post_likes（点赞关系）

| 字段名     | 类型     | 长度 | 是否为空 | 键                | 注释                              |
| ---------- | -------- | ---- | -------- | ----------------- | --------------------------------- |
| id         | BIGINT   | -    | 否       | PK, 自增          | 点赞主键ID                        |
| post_id    | BIGINT   | -    | 否       | FK, UK(联合), IDX | 帖子ID（关联 community_posts.id） |
| username   | VARCHAR  | 100  | 否       | UK(联合)          | 点赞用户名                        |
| created_at | DATETIME | -    | 否       | -                 | 创建时间                          |
| updated_at | DATETIME | -    | 否       | -                 | 更新时间                          |

#### 9) user_follows（关注关系）

| 字段名             | 类型     | 长度 | 是否为空 | 键                | 注释                                  |
| ------------------ | -------- | ---- | -------- | ----------------- | ------------------------------------- |
| id                 | BIGINT   | -    | 否       | PK, 自增          | 关注关系主键ID                        |
| follower_username  | VARCHAR  | 100  | 否       | FK, UK(联合), IDX | 关注者用户名（关联 users.username）   |
| following_username | VARCHAR  | 100  | 否       | FK, UK(联合), IDX | 被关注者用户名（关联 users.username） |
| created_at         | DATETIME | -    | 否       | -                 | 创建时间                              |
| updated_at         | DATETIME | -    | 否       | -                 | 更新时间                              |

#### 10) user_messages（私信消息）

| 字段名            | 类型     | 长度 | 是否为空 | 键            | 注释                                |
| ----------------- | -------- | ---- | -------- | ------------- | ----------------------------------- |
| id                | BIGINT   | -    | 否       | PK, 自增      | 私信主键ID                          |
| sender_username   | VARCHAR  | 100  | 否       | FK, IDX(复合) | 发送者用户名（关联 users.username） |
| receiver_username | VARCHAR  | 100  | 否       | FK, IDX(复合) | 接收者用户名（关联 users.username） |
| content           | VARCHAR  | 2000 | 否       | -             | 私信内容                            |
| read_by_receiver  | BIT      | -    | 否       | IDX(复合)     | 接收方是否已读（默认0）             |
| created_at        | DATETIME | -    | 否       | IDX(复合)     | 创建时间                            |
| updated_at        | DATETIME | -    | 否       | -             | 更新时间                            |

#### 11) content_reports（举报工单）

| 字段名              | 类型     | 长度 | 是否为空 | 键       | 注释                                |
| ------------------- | -------- | ---- | -------- | -------- | ----------------------------------- |
| id                  | BIGINT   | -    | 否       | PK, 自增 | 举报单主键ID                        |
| reporter_username   | VARCHAR  | 100  | 否       | FK, IDX  | 举报人用户名（关联 users.username） |
| target_type         | VARCHAR  | 20   | 否       | -        | 举报目标类型                        |
| target_id           | BIGINT   | -    | 否       | -        | 举报目标ID                          |
| reason              | VARCHAR  | 120  | 否       | -        | 举报原因                            |
| details             | VARCHAR  | 2000 | 是       | -        | 举报详情                            |
| status              | VARCHAR  | 20   | 否       | IDX      | 工单状态（默认PENDING）             |
| handled_by_username | VARCHAR  | 100  | 是       | FK       | 处理人用户名（关联 users.username） |
| handled_at          | DATETIME | -    | 是       | -        | 处理时间                            |
| handle_note         | VARCHAR  | 1000 | 是       | -        | 处理备注                            |
| created_at          | DATETIME | -    | 否       | -        | 创建时间                            |
| updated_at          | DATETIME | -    | 否       | -        | 更新时间                            |

#### 12) user_account_appeals（账号申诉）

| 字段名              | 类型     | 长度 | 是否为空 | 键       | 注释                                |
| ------------------- | -------- | ---- | -------- | -------- | ----------------------------------- |
| id                  | BIGINT   | -    | 否       | PK, 自增 | 账号申诉主键ID                      |
| username            | VARCHAR  | 100  | 否       | FK, IDX  | 申诉用户名（关联 users.username）   |
| appeal_type         | VARCHAR  | 40   | 否       | -        | 申诉类型                            |
| details             | VARCHAR  | 2000 | 否       | -        | 申诉详情                            |
| status              | VARCHAR  | 20   | 否       | IDX      | 申诉状态（默认PENDING）             |
| handled_by_username | VARCHAR  | 100  | 是       | FK       | 处理人用户名（关联 users.username） |
| handled_at          | DATETIME | -    | 是       | -        | 处理时间                            |
| handle_note         | VARCHAR  | 1000 | 是       | -        | 处理备注                            |
| created_at          | DATETIME | -    | 否       | -        | 创建时间                            |
| updated_at          | DATETIME | -    | 否       | -        | 更新时间                            |

### 4.3.3 主要关系设计

1. users 与 pets

- 一对多（一个用户可拥有多只宠物）。

2. pets 与 memory_entries / pet_archive_records / community_posts

- 一对多。
- 删除宠物时级联删除关联回忆、档案、帖子，避免孤儿数据。

3. community_posts 与 community_comments / community_post_likes

- 一对多。
- 帖子删除时级联删除评论和点赞关系。

4. users 与 user_follows / user_messages / 举报申诉

- 通过用户名外键维持社交和治理链路一致性。

### 4.3.4 索引与性能设计

系统针对高频场景配置了索引：

1. 信息流场景

- community_posts(created_at DESC, id DESC)

2. 评论场景

- community_comments(post_id, created_at ASC, id ASC)

3. 时间线场景

- memory_entries(pet_id, event_date DESC, id DESC)

4. 提醒场景

- pet_archive_records(reminder_enabled, reminder_status, reminder_at)

5. 社交场景

- user_follows(follower_username)、user_follows(following_username)

### 4.3.5 一致性与约束策略

1. 唯一性约束

- users.username、users.email 唯一。
- pets.share_token 唯一。
- community_post_likes(post_id, username) 唯一。
- user_follows(follower_username, following_username) 唯一。

2. 级联策略

- 宠物、帖子相关子记录采用 ON DELETE CASCADE，保证删除闭环。

3. 字段校验策略

- 后端 DTO 校验 + 数据库字段长度约束双重兜底。
- 示例：生日为过去或当天；名称、简介、备注均有长度边界。

## 4.4 接口设计

### 4.4.1 接口设计规范

1. 基础规范

- 协议：HTTP/HTTPS。
- 风格：RESTful。
- 基础路径：/api。

2. 统一响应结构

- 全部业务接口采用统一结构：
- code：业务码（成功为 200）
- message：提示信息
- data：业务数据

3. 统一错误处理

- 参数错误：400。
- 未认证：401。
- 无权限：403。
- 资源不存在：404。
- 服务器异常：500。

### 4.4.2 鉴权与权限控制

1. JWT 认证

- 前端请求头携带 Authorization: Bearer `<token>`。
- 后端过滤器校验并写入安全上下文。

2. 角色权限

- ROLE_ADMIN：可访问 /api/admin/** 全部治理接口。
- ROLE_USER：可访问业务写接口。
- ROLE_GUEST：仅可访问社区只读接口。

3. 公共接口

- /api/auth/**、/api/public/**、/api/test/**、POST /api/account-appeals/public 允许匿名。
- /uploads/** 允许 GET 公开访问。

### 4.4.3 核心接口分组

#### 1) 认证与用户

| 方法 | 路径               | 说明         |
| ---- | ------------------ | ------------ |
| POST | /api/auth/register | 用户注册     |
| POST | /api/auth/login    | 用户登录     |
| POST | /api/auth/guest    | 游客登录     |
| GET  | /api/auth/me       | 获取当前用户 |
| GET  | /api/users/me      | 获取个人资料 |
| PUT  | /api/users/me      | 更新个人资料 |

#### 2) 宠物与回忆

| 方法   | 路径                       | 说明         |
| ------ | -------------------------- | ------------ |
| GET    | /api/pets                  | 查询我的宠物 |
| GET    | /api/pets/{id}             | 宠物详情     |
| POST   | /api/pets                  | 新建宠物     |
| PUT    | /api/pets/{id}             | 编辑宠物     |
| DELETE | /api/pets/{id}             | 删除宠物     |
| GET    | /api/pets/{petId}/memories | 查询宠物回忆 |
| GET    | /api/memories/{id}         | 回忆详情     |
| POST   | /api/pets/{petId}/memories | 新建回忆     |
| PUT    | /api/memories/{id}         | 编辑回忆     |
| DELETE | /api/memories/{id}         | 删除回忆     |

#### 3) 宠物档案与提醒

| 方法   | 路径                                     | 说明         |
| ------ | ---------------------------------------- | ------------ |
| GET    | /api/pets/{petId}/archives               | 查询档案     |
| POST   | /api/pets/{petId}/archives               | 新建档案     |
| PUT    | /api/pet-archives/{id}                   | 编辑档案     |
| DELETE | /api/pet-archives/{id}                   | 删除档案     |
| GET    | /api/pet-archives/reminders/due          | 查询到期提醒 |
| POST   | /api/pet-archives/{id}/reminder/complete | 标记提醒完成 |
| POST   | /api/pet-archives/{id}/reminder/snooze   | 提醒稍后     |

#### 4) 社区与社交

| 方法   | 路径                                   | 说明                           |
| ------ | -------------------------------------- | ------------------------------ |
| GET    | /api/community/feed                    | 社区信息流（游客可读）         |
| GET    | /api/community/following-feed          | 关注信息流（仅用户/管理员）    |
| GET    | /api/community/recommendations         | 推荐内容（游客可读）           |
| GET    | /api/community/hot-pets                | 宠物热榜（游客可读）           |
| GET    | /api/community/mine                    | 我的帖子（仅用户/管理员）      |
| POST   | /api/community/posts                   | 发布帖子（仅用户/管理员）      |
| GET    | /api/community/topics                  | 话题列表（游客可读）           |
| POST   | /api/community/topics                  | 创建话题（仅用户/管理员）      |
| POST   | /api/community/posts/{postId}/like     | 点赞/取消点赞（仅用户/管理员） |
| GET    | /api/community/posts/{postId}/comments | 评论列表（游客可读）           |
| POST   | /api/community/posts/{postId}/comments | 新增评论（仅用户/管理员）      |
| POST   | /api/social/follow/{username}          | 关注用户                       |
| DELETE | /api/social/follow/{username}          | 取消关注                       |
| GET    | /api/social/following                  | 我的关注                       |
| GET    | /api/social/followers                  | 我的粉丝                       |
| GET    | /api/social/summary                    | 关注汇总                       |

#### 5) 私信、举报、申诉、管理

| 方法 | 路径                                        | 说明                                  |
| ---- | ------------------------------------------- | ------------------------------------- |
| GET  | /api/messages/conversations                 | 会话列表                              |
| GET  | /api/messages/with/{username}               | 与指定用户会话                        |
| POST | /api/messages/with/{username}               | 发送私信                              |
| PUT  | /api/messages/with/{username}/read          | 标记会话已读                          |
| GET  | /api/messages/summary                       | 私信汇总                              |
| POST | /api/reports                                | 创建举报                              |
| GET  | /api/reports/mine                           | 我的举报                              |
| GET  | /api/reports/admin                          | 管理员查询举报                        |
| PUT  | /api/reports/admin/{id}/handle              | 管理员处理举报                        |
| POST | /api/account-appeals                        | 用户提交申诉                          |
| POST | /api/account-appeals/public                 | 冻结用户公共申诉入口                  |
| GET  | /api/account-appeals/mine                   | 我的申诉                              |
| GET  | /api/admin/users                            | 管理员查询用户                        |
| PUT  | /api/admin/users/{id}/status                | 管理员更新用户状态（冻结/限制发布等） |
| POST | /api/admin/users/{id}/warn                  | 管理员警告用户                        |
| GET  | /api/admin/account-appeals                  | 管理员查询申诉                        |
| PUT  | /api/admin/account-appeals/{id}/handle      | 管理员处理申诉                        |
| GET  | /api/admin/community/posts                  | 管理员查询帖子                        |
| GET  | /api/admin/community/comments               | 管理员查询评论                        |
| PUT  | /api/admin/community/posts/{id}/moderate    | 管理员审核帖子                        |
| PUT  | /api/admin/community/comments/{id}/moderate | 管理员审核评论                        |
| GET  | /api/admin/dashboard/overview               | 管理概览                              |

#### 6) 公开访问与文件上传

| 方法 | 路径                                   | 说明           |
| ---- | -------------------------------------- | -------------- |
| GET  | /api/public/pets/{shareToken}          | 单宠物公开页   |
| GET  | /api/public/users/search               | 公开用户搜索   |
| GET  | /api/public/users/{ownerUsername}/home | 用户公开主页   |
| GET  | /uploads/{fileName}                    | 访问上传文件   |
| POST | /api/files/upload                      | 上传图片或视频 |
| POST | /api/files/upload-image                | 上传图片       |

### 4.4.4 接口设计小结

接口体系以“统一返回结构 + 角色权限边界 + 模块化路径命名”为核心，既能满足毕业设计的完整业务闭环，也保留了后续扩展空间（例如推荐参数化、对象存储替换、接口版本化等）。
