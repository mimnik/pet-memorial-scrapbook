# 5 系统核心功能实现

本章基于第4章的功能设计，从技术实现角度阐述系统的核心功能模块，重点说明用户认证与权限控制、宠物私人纪念册以及宠物微社区模块的具体实现方案。其中，宠物微社区模块作为本系统的创新核心，将详细展开其四项创新特性的技术实现细节。

### 5.1 开发环境与项目结构

本节简要说明系统的技术选型和项目组织方式。

**1. 技术栈总览**

| 领域 | 技术与框架 | 版本/说明 |
| :--- | :--- | :--- |
| **后端** | Spring Boot | 3.3.5 (核心应用框架) |
| | Java JDK | 21 (选用LTS版本提供的高效特性) |
| | Spring Security + JWT | 权限控制与无状态令牌 (jjwt 0.12.6) |
| | Spring Data JPA | ORM映射与数据持久化 |
| | MySQL | 8.4.0 (统一数据源，全面弃用H2) |
| **前端** | Vue 3 | 3.5.27 (采用 Composition API + setup) |
| | Element Plus | 2.13.4 (核心 UI 组件库) |
| | Pinia & Vue Router | 3.0.4 & 5.0.3 (状态管理与基于角色的路由) |
| | Vite & TypeScript | 7.3.1 (极速构建与强类型检查) |
| **部署** | Docker Compose + Nginx | 容器化部署编排与反向代理映射 |

**2. 项目目录结构**

```text
PetMemorialScrapbook/
├── backend/                  # 后端工程目录
│   ├── src/main/java/...     # 核心Java业务代码 (分层: controller/service/repository)
│   ├── src/main/resources/   # 配置文件与静态资源
│   └── pom.xml               # Maven项目依赖配置
├── frontend/                 # 前端工程目录
│   ├── src/api/              # 集中管理的Axios网络请求
│   ├── src/views/            # 页面级视图组件
│   ├── src/components/       # 可复用业务组件
│   └── package.json          # Node.js依赖与脚本
├── database/                 # 数据库脚本 (init.sql, schema.sql)
└── deploy/                   # 生产化部署配置 (Dockerfile, docker-compose.yml, nginx.conf)
```

**3. 核心配置文件说明**

后端核心配置文件 `application.yml` 通过环境变量注入机制实现了高度灵活的部署覆盖：
- **数据库连接**：依赖 `${DB_URL}`、`${DB_USERNAME}` 等环境变量，便于从本地直连切换到容器内部网络。
- **文件上传限制**：通过 `spring.servlet.multipart.max-file-size: 1024MB` 放宽限制，专门应对宠物纪念视频的上传。上传落盘目录受 `${UPLOAD_DIR}` 控制。
- **安全配置**：JWT 密钥由 `${JWT_SECRET}` 注入（强制要求至少 32 字符进行 HS256 签名），Token 默认有效期设为 24 小时（86400000 毫秒）。

---

### 5.2 用户认证与权限控制实现

本节阐述基于 Spring Security 和 JWT 的三级角色（ADMIN / USER / GUEST）隔离机制的实现方案。

#### 5.2.1 JWT令牌签发与校验

JWT 令牌采用标准的 Header、Payload、Signature 三段式结构。载荷（Payload）中封装了 `username`、`role` 与有效时间。采用 HS256 加密算法保证其不可被篡改。

```java
// JwtTokenUtils.java 核心代码片段
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
    
    return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact();
}
```

#### 5.2.2 Spring Security配置

通过定制 `SecurityFilterChain`，本系统禁用了传统的 Session，转而采用纯无状态（`STATELESS`）的策略配置。

```java
// SecurityConfig.java 核心代码片段
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/login", "/api/auth/guest", "/api/auth/register", "/api/public/**", "/uploads/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```
结合使用 `BCryptPasswordEncoder` 防止数据库明文密码泄露。

#### 5.2.3 三级角色权限隔离

除了方法外的 URL 规则，微调细粒度的管控由 `@PreAuthorize` 承担：
- `@PreAuthorize("hasRole('ADMIN')")`：挂载在 `AdminController` 的风控、封号、举报审理接口。
- `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")`：挂载在发帖、私信等需要实体确权写操作的方法上。

#### 5.2.4 游客登录实现

游客机制旨在降低用户体验门槛。接口接收请求后，**不执行数据库查询**，而是在内存中生成一个带 `guest_` 前缀的 UUID 虚拟账号，强制指定 `ROLE_GUEST` 角色进行 JWT 签发。

```java
// AuthController.java (游客登录段)
@PostMapping("/guest")
public ResponseEntity<?> guestLogin() {
    String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
    // 构建内存级 UserDetails 并注入 ROLE_GUEST
    UserDetails guestUser = User.withUsername(guestId).password("").roles("GUEST").build();
    String token = jwtTokenUtils.generateToken(guestUser);
    return ResponseEntity.ok(new AuthResponse(token, guestId, "ROLE_GUEST"));
}
```
前端收到后将其视同普通令牌，但受限于后端无权访问写接口逻辑，系统平滑实现了“只读浏览社区”的需求。

---

### 5.3 宠物私人纪念册实现

本节阐述宠物档案管理和回忆录管理的核心实现。

#### 5.3.1 宠物档案的CRUD操作

在 JPA 实体设计中，`Pet` 类对应 `pets` 表，与回忆记录、提醒档案呈现典型的一对多映射。使用 `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` 确保删除宠物时，不仅连带删除其关联的 `memory_entries` 和 `pet_archive_records`，还能避免产生孤儿数据。

#### 5.3.2 回忆录与文件上传

`MemoryEntry` 实体定义了 `imageUrl` 和 `videoUrl` 字段支持富媒体沉淀。采用原生的 `MultipartFile` 进行文件流承接与安全落盘校验。

```java
// FileUploadService.java
public String storeFile(MultipartFile file) {
    // 校验文件类型及规范名
    String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
    String newFileName = UUID.randomUUID() + "_" + originalFilename;
    Path targetLocation = Paths.get(uploadDir).resolve(newFileName);
    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    // 返回可访问的相对URL
    return "/uploads/" + newFileName;
}
```

#### 5.3.3 时间线聚合查询

为形成“时光机”记忆流体验，后端利用 Spring Data JPA 派生查询：
```java
List<MemoryEntry> findByPetIdOrderByEventDateDesc(Long petId);
```
在原生 SQL 表现为 `SELECT * FROM memory_entries WHERE pet_id = ? ORDER BY event_date DESC`。前端获取到 Array 后，再通过遍历按 `YYYY-MM` 的键提取组装成 Map 对象，进而渲染组件库中的时间轴 `<el-timeline>`。

#### 5.3.4 健康提醒定时任务

利用 Spring 的 `@Scheduled` 配合 cron 表达式实现异步待办扫表：

```java
// ReminderScheduleTask.java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
public void checkAndGenerateReminders() {
    List<PetArchiveRecord> dueRecords = archiveRepository.findByReminderEnabledTrueAndReminderStatus(ReminderStatus.PENDING);
    for(PetArchiveRecord record : dueRecords) {
        if(record.getReminderAt().isBefore(LocalDateTime.now())) {
            // 派发站内信或消息推送处理逻辑
            // ...
        }
    }
}
```

---

### 5.4 宠物微社区模块实现（本章重点）

宠物微社区模块是本系统的创新核心，本节从技术实现角度详细阐述四项创新特性以及社区互动与内容分发机制的具体实现方案。

#### 5.4.1 双时态叙事与情绪天气标签

**双时态叙事的技术实现**
发帖时采集 `narrativeMode`（枚举 `DAILY` 或 `MEMORIAL`）。Vue 3 前端基于此字段计算类名实现界面情感降级与升格切换。
```vue
<!-- 前端动态样式绑定 -->
<div :class="['post-card', post.narrativeMode === 'MEMORIAL' ? 'memorial-theme' : 'daily-theme']">
   <!-- 纪念模式下挂载灰色滤镜 (filter: grayscale) -->
</div>
```

**情绪天气标签的实现**
采用 `moodTag`（例如 `SUNNY`、`RAINY`、`LOVE`、`MISS`）做色彩情绪定调。
```typescript
// mood-configs.ts 返回色彩映射与ICON
const moodMap: Record<string, { color: string, icon: string, text: string }> = {
  'SUNNY': { color: '#FFF9C4', icon: 'Sunny', text: '晴天' },
  'RAINY': { color: '#E3F2FD', icon: 'Pouring', text: '雨天' }
};
const currentMood = computed(() => moodMap[props.post.moodTag] || moodMap['SUNNY']);
```

#### 5.4.2 宠物第一视角开关

**实现逻辑**
数据库新增布尔型 `pet_voice` 字段。前端在渲染卡片信息时采用拦截器模式（或 Computed属性）重写发帖人的基础资料。
```typescript
const displayAvatar = computed(() => {
  return props.post.petVoice && props.post.pet 
         ? props.post.pet.avatarUrl 
         : props.post.authorAvatarUrl;
});

const displayName = computed(() => {
  return props.post.petVoice && props.post.pet 
         ? `${props.post.pet.name} (的主人)` 
         : props.post.authorDisplayName;
});
```
这种设计只需操作前端的视图模型，既不污染原始用户映射表，又提供了强烈的“宠物在对话”的同理交互。

#### 5.4.3 纪念接力挑战

**数据结构设计**
通过建立闭环自引用的关联：`relay_enabled`（布尔值）和 `relay_parent_id`（指向另一帖子的主键ID）。
根帖（即发起的挑战发起者）：`relay_parent_id = NULL`, `relay_enabled = true`。
以此形成链表数据。为便于统一溯源追踪整条时间链，后台维护 `relay_chain_id` （始终记录顶层帖ID）和 `relay_depth`（当前层级）。

**接力查询算法**
采用 CTE 递归公共表表达式（MySQL 8.0+ 支持）来还原链路或者在业务层通过 `relay_chain_id` 全局聚合拉取：
```sql
SELECT * FROM community_posts WHERE relay_chain_id = 101 ORDER BY relay_depth ASC;
```
前端借此便能使用递归树状组件 `<RelayTree>` 进行链路渲染。

#### 5.4.4 社区互动与内容分发

**点赞与评论功能**
结构上点赞表采用独立映射 `community_post_likes` 包含 `post_id`、`user_id` 和 `created_at`。并在两者之间建立唯一联合索引(`UNIQUE KEY (post_id, username)`)。利用后端 JPA 的 `existsByPostIdAndUsername` 校验，有则解绑删除（取消赞），无则新增（点赞），实现全流程幂等交互。

**自定义推荐算法与宠物热榜**
系统基于“红迪网(Reddit)”热度算法变体，按需处理热榜的分发：
$$ 热度值 (Hot Score) = \log_{10}(\text{点赞数} + \text{评论数} + 1) + \frac{T_{post} - T_{base}}{45000} $$
*衰减系数（45000）控制新帖的权重视重。* 后端每日定时更新整库的 `hot_score` 映射列，在流调用中支持动态 `ORDER BY hot_score DESC` 展现出具有热效期的分发排序。

**公开链接与分享功能**
针对“炫宠”需求，系统分离出了不需要JWT干预的公开分享模块模块 `/home/:ownerUsername` 或基于 `share_token` 定向分享单宠。
```java
// PublicController.java 安全隔离访问
@GetMapping("/public/users/{ownerUsername}/home")
public ResponseEntity<?> getPublicHome(@PathVariable String ownerUsername) {
    // 强制追加 is_public = true 规避隐私泄露
    List<Pet> publicPets = petRepository.findByOwnerUsernameAndIsPublicTrue(ownerUsername);
    return ResponseEntity.ok(publicPets);
}
```
结合前端 Web App 的 Router 的自由控制生成分享专属地址，极大增强了系统的社交自发式引流能力。