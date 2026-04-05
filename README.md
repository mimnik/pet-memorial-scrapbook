# Pet Memorial Scrapbook

一个基于 Spring Boot + Vue 3 的宠物数字纪念册项目，支持宠物档案、回忆时间线、公开主页、宠物微社区（搜索/推荐/热榜）与图文视频内容沉淀。

## 项目状态

当前仓库已完成可运行的毕业设计增强版本：

- 用户注册/登录/JWT 鉴权与路由守卫。
- 登录失败支持精确提示（账户不存在/密码错误）。
- 宠物与回忆完整 CRUD。
- 宠物信息中的性别字段已改为选项输入，减少自由文本歧义。
- 图片与视频上传（单文件上限 1024MB，支持视频时长与封面）。
- 我的宠物、回忆时间线、社区动态均支持图片直显与视频内容展示。
- 公开分享：
  - Token 分享页：/share/:token
  - 用户主页后缀页：/home/:ownerUsername?pet=shareToken
- 宠物微社区：
  - 动态发布弹窗
  - 搜索（标题/正文/宠物名/用户名）
  - 内容推荐（自定义评分）
  - 宠物热榜
  - 评论/点赞/纪念接力

## 本轮需求完成说明（2026-03-28）

- 已完成登录错误精确提示的前后端联动。
- 已完成回忆与社区的视频上传、封面生成、时长展示与 1024MB 限制。
- 已完成用户主页后缀公开链接模式，支持不同用户独立公开首页。
- 已完成社区搜索栏、推荐栏、热榜栏和弹窗发布流程。
- 已完成前端页面样式优化与交互细节提升。

## 技术栈

- 后端：Spring Boot 3.3.5、Spring Security、Spring Data JPA、H2（默认）/MySQL（可选）
- 前端：Vue 3、TypeScript、Vite 7、Element Plus、Pinia、Vue Router、Axios
- 运维：PowerShell 一键启停脚本，FRP 前端远程映射（可选）

## 目录结构

- backend：Spring Boot 服务
- frontend：Vue 前端工程
- docs：项目文档与需求文档
- scripts：一键启动/停止脚本
- database：数据库脚本目录
- deploy：部署配置目录

## 快速开始（Windows）

在项目根目录执行：

- 一键启动（默认 H2）：
  - powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1
- 一键启动（MySQL）：
  - powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1 -UseMySQL
- 一键停止：
  - powershell -ExecutionPolicy Bypass -File .\scripts\stop-dev.ps1

说明：

- 启动脚本会构建后端并拉起后端 8080、前端 3000。
- 如已配置 FRP，脚本可一并启动 frpc（前端公网映射）。
- 脚本默认清理占用端口，减少重复启动失败。

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 20.19+ 或 22.12+
- npm 10+
- MySQL 8+（可选，仅 mysql profile 需要）

## 后端配置要点

`backend/src/main/resources/application.yml` 支持：

- JWT_SECRET
- JWT_EXPIRATION
- DB_URL / DB_USERNAME / DB_PASSWORD（mysql profile）
- UPLOAD_DIR（上传目录）

已配置 multipart 上限为 1024MB。

## 关键接口

基础路径：/api

认证：

- POST /auth/register
- POST /auth/login
- GET /auth/me

宠物：

- GET /pets
- GET /pets/{id}
- POST /pets
- PUT /pets/{id}
- DELETE /pets/{id}

回忆：

- GET /pets/{petId}/memories
- POST /pets/{petId}/memories
- PUT /memories/{id}
- DELETE /memories/{id}

社区：

- GET /community/feed?mode=&mood=&keyword=
- GET /community/recommendations?limit=
- GET /community/hot-pets?limit=
- GET /community/mine
- POST /community/posts
- POST /community/posts/{postId}/like
- GET /community/posts/{postId}/comments
- POST /community/posts/{postId}/comments

公开页：

- GET /public/pets/{shareToken}
- GET /public/users/{ownerUsername}/home?pet=

上传：

- POST /files/upload（图片或视频）
- POST /files/upload-image（仅图片）

## 测试与构建

后端：

- 编译：cd backend && mvnw.cmd -DskipTests compile
- 测试：cd backend && mvnw.cmd test

前端：

- 构建：cd frontend && npm run build
- 单测：cd frontend && npm run test:unit -- --run

## 文档

- 项目文档：docs/PROJECT_DOCUMENTATION.md
- 需求文档：docs/REQUIREMENTS.md
