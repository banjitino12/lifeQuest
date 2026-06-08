# LifeQuest Current Process

本文档用于记录 LifeQuest 项目的当前开发进度、已完成工作和后续 TODO。每次项目取得进展后，都应同步更新本文档。

## 当前阶段

项目处于前后端基础框架搭建阶段。

当前重点是：

- 固化项目规则和协作约束
- 明确前后端分离目录结构
- 建立文档、基础设施和脚本目录
- 完成 Spring Boot 后端基础配置、模块边界和数据库迁移脚本
- 完成 Vue 3 + TypeScript 前端基础配置、入口文件和路由配置
- 为后续前端基础布局、页面骨架和后端业务实现做准备

## 已完成工作

### 2026-06-08

- 创建项目基础目录结构：
  - `backend/`
  - `frontend/`
  - `docs/`
  - `infra/`
  - `scripts/`
  - `.github/workflows/`
- 在空目录中添加 `.gitkeep`，确保目录可以被 Git 追踪。
- 更新 `README.md`：
  - 添加项目介绍
  - 添加核心定位
  - 添加 MVP 功能范围
  - 添加基础技术架构
  - 添加项目结构说明
  - 添加后端模块说明
  - 添加数据设计方向
  - 添加 LLM 设计原则
  - 添加项目边界
- 创建 `agent.md`：
  - 记录每次构建前必须阅读的文件
  - 记录不可破坏的文件和目录
  - 记录 LifeQuest 核心项目原则
  - 记录前后端架构约束
  - 记录开发、文档、测试规则
  - 记录禁止事项和默认工作流程
- 创建 `docs/current_process.md`，用于持续记录项目进度和后续 TODO。
- 更新 `agent.md`，要求每次构建项目前必须阅读 `docs/current_process.md`，并在项目取得进展后更新该文件。
- 整理原始需求文档到 `docs/requirements/product_requirements.md`：
  - 梳理项目概述、目标用户、核心问题和创新点
  - 梳理用户、每日记录、评分、属性、游戏化、LLM、可视化等功能需求
  - 梳理非功能需求、技术栈建议、MVP 范围、用户流程、页面需求、数据表设计和验收标准
- 补充系统架构设计到 `docs/architecture/system_architecture.md`：
  - 明确前后端分离总体架构
  - 梳理前端、后端、数据存储和 LLM 模块职责
  - 设计核心业务流程、异步降级流程、安全隐私和可测试性原则
- 补充数据库表结构设计到 `docs/database/database_design.md`：
  - 明确 MySQL、MongoDB、Redis 的存储职责
  - 设计用户、目标、路线、每日记录、评分、属性、游戏事件、LLM 反馈、明日任务和周报等表
  - 补充索引、枚举、隐私隔离、迁移脚本和 MVP 建表优先级
- 补充 MVP 接口设计到 `docs/api/mvp_api_design.md`：
  - 设计认证、用户、目标配置、成长首页、每日记录、每日结算、任务、路线、趋势、周报、LLM 解析和字典接口
  - 明确通用响应、错误码、鉴权、参数校验、隐私规则和接口优先级
- 更新 `.gitignore`，忽略 macOS 自动生成的 `.DS_Store` 文件和本地 IDE 配置目录。
- 初始化后端 Spring Boot Maven 工程：
  - 新增 `backend/pom.xml`
  - 配置 Spring Boot、Java 17、Web、Validation、Security、Actuator、JPA、MongoDB、Redis、Flyway、MySQL、JWT 和测试依赖
  - 新增后端应用入口 `LifeQuestApplication`
  - 新增 `application.yml` 和 `application-local.yml` 基础配置
  - 新增 Spring Boot 上下文测试骨架
  - 新增 `application-test.yml`，让基础测试不依赖本地 MySQL、MongoDB 和 Redis
  - 更新 `.gitignore`，忽略 Maven `target/` 构建产物
  - 已通过 `mvn test` 验证 Maven 配置、应用入口和测试 profile 可用
- 建立后端统一响应结构和异常处理骨架：
  - 新增 `ApiResponse`，统一接口响应字段 `code`、`message`、`data`
  - 新增 `ErrorCode`，集中维护错误码、默认消息和 HTTP 状态
  - 新增 `BusinessException`，作为业务层主动抛出的统一异常
  - 新增 `ValidationErrorDetail`，承载参数校验错误详情
  - 新增 `GlobalExceptionHandler`，统一处理业务异常、参数校验、鉴权、权限、资源状态和未知异常
  - 新增 `ApiResponseTests`，覆盖统一响应对象基础行为
- 建立后端核心业务模块基础类结构：
  - `auth`：注册、登录请求/响应 DTO、Controller 和 Service 骨架
  - `user`：当前用户响应 DTO、Controller 和 Service 骨架
  - `profile`：目标配置请求/响应 DTO、Controller 和 Service 骨架
  - `dailylog`：每日记录提交请求、摘要响应、Controller 和 Service 骨架
  - `scoring`：评分结果模型和规则评分 Service 骨架
  - `attribute`：属性快照、属性变化模型和 Service 骨架
  - `gamification`：游戏事件模型和 Service 骨架
  - `llm`：自然语言日志解析结果模型、LLM Service 接口和降级实现骨架
  - `route`：路线摘要 DTO、Controller 和 Service 骨架
  - `trend`：趋势摘要 DTO、Controller 和 Service 骨架
  - `weekly`：周报摘要 DTO、Controller 和 Service 骨架
- 设计 MySQL 数据表迁移脚本：
  - 新增 `V1__create_user_and_profile_tables.sql`，创建用户和用户目标配置表
  - 新增 `V2__create_route_tables.sql`，创建成长路线、章节、关卡和用户路线进度表
  - 新增 `V3__create_daily_log_and_score_tables.sql`，创建每日记录和每日评分表
  - 新增 `V4__create_attribute_and_game_event_tables.sql`，创建用户属性、属性变化、游戏事件、成就和用户成就表
  - 新增 `V5__create_llm_and_report_tables.sql`，创建 LLM 反馈、明日任务和周报表
  - 迁移脚本包含主键、唯一约束、外键、索引、枚举 CHECK 和范围 CHECK
- 初始化 Vue 3 + TypeScript 前端工程配置：
  - 新增 `frontend/package.json` 和 `frontend/package-lock.json`
  - 配置 Vite、Vue 3、TypeScript、Vue Router、Pinia、Axios、Element Plus 和 ECharts 基础依赖
  - 新增 `vite.config.ts`，配置 `@` 路径别名、开发端口 `5173` 和 `/api` 到后端 `8080` 的代理
  - 新增 `tsconfig.json`、`tsconfig.app.json`、`tsconfig.node.json` 和 `env.d.ts`
  - 更新 `.gitignore`，忽略 `node_modules/`、`dist/`、`coverage/`、本地环境文件和 TypeScript 增量缓存
- 创建前端入口文件和路由配置：
  - 新增 `src/main.ts`，挂载 Vue 应用并注册 Router、Pinia 和 Element Plus
  - 新增 `src/App.vue`，提供基础应用外壳、侧边导航和 `RouterView`
  - 新增 `src/router/index.ts`，配置登录、注册、成长首页、每日记录、每日结算、成长路线、趋势和周报路由
  - 新增 `src/styles/main.css`，提供基础全局样式和响应式布局
  - 新增各核心页面的最小占位视图，保证路由懒加载链路可运行
  - 已通过 `npm run build` 验证前端类型检查和生产构建可用
- 建立后端 JPA Entity、Repository 和基础枚举：
  - 新增通用实体基类 `BaseEntity` 和 `CreatedOnlyEntity`
  - 新增账号状态、目标类型、反馈风格、每日记录来源、每日评级、事件类型、LLM 类型与状态、任务类型与状态、路线进度状态、周报状态等基础枚举
  - 完成 `user`、`user_profile`、`daily_log`、`daily_score`、`user_attribute`、`attribute_change`、`game_event`、`growth_route`、`route_chapter`、`route_level`、`user_route_progress`、`llm_feedback`、`tomorrow_task`、`weekly_report` 的 JPA Entity
  - 完成对应 Repository，并为用户业务数据提供 `userId` 维度的查询方法
  - 测试环境关闭 JPA、Mongo、Redis 等外部资源自动连接，避免基础测试依赖本地服务
  - 新增实体映射和仓储契约验证测试，覆盖表名、关键列名、`user_id` 隔离字段、枚举值和 Repository 查询入口
  - 已通过 `mvn test` 验证后端编译、Spring Boot 测试上下文、统一响应测试、实体映射测试和仓储契约测试
- 完成后端认证与安全基础逻辑：
  - 实现用户注册、登录、刷新 Token 和退出登录接口
  - 注册逻辑支持用户名、邮箱、手机号唯一性校验，并要求邮箱和手机号至少填写一个
  - 登录逻辑支持通过用户名、邮箱或手机号匹配账号，并使用 BCrypt 校验密码
  - 新增 JWT 配置、Token 生成、Token 解析和 access / refresh token 类型校验
  - 新增 `CurrentUserPrincipal` 和 `CurrentUserService`，为后续业务模块提供当前登录用户 ID
  - 配置 Spring Security：注册、登录、刷新 Token 和健康检查开放，其他接口默认需要 JWT 鉴权
  - 新增 JWT 认证过滤器，从 Token 解析用户身份并校验用户状态
  - 新增统一安全错误响应，未登录返回 `UNAUTHORIZED`，无权限返回 `FORBIDDEN`
  - 更新 `/users/me`，从 SecurityContext 当前用户读取数据，不接受前端传入的 `userId`
  - 新增认证服务测试和安全集成测试，覆盖注册放行、登录发放 Token、受保护接口 401、JWT 解析当前用户、刷新 Token 类型校验
  - 已通过 `mvn test` 验证后端 16 个测试全部成功
- 更新项目协作规则：
  - 在 `agent.md` 中新增后端变更后的 API 文档同步规则
  - 要求每次后端发生接口、鉴权、响应、错误码、用户身份来源、业务流程或模块边界变化时，检查 `docs/api/mvp_api_design.md`
  - 如 API 设计文档与实际后端实现不一致，必须在同次修改中更新 `docs/api/mvp_api_design.md`
- 同步认证与安全接口文档：
  - 更新 `docs/api/mvp_api_design.md` 中注册接口的响应 `message` 和 `profileCompleted` 字段
  - 更新刷新 Token 接口响应，使其与当前 `AuthTokenResponse` 保持一致
  - 明确 `/auth/refresh` 无需鉴权，`/auth/logout` 需要 JWT 鉴权
  - 明确 `/auth/logout` 当前为 JWT stateless 基础退出，Redis Token 黑名单后续再增强
  - 按当时后端实现状态标注用户信息更新接口
- 完成用户与目标配置逻辑：
  - 实现 `PATCH /api/users/me`，支持当前登录用户更新用户名和头像
  - 用户资料更新从 JWT 当前用户解析 `userId`，不接收前端传入的 `userId`
  - 用户名更新时校验唯一性，避免与其他用户冲突
  - 实现 `GET /api/profile/me`，未配置时返回 `completed=false` 的空配置，已配置时返回目标、反馈风格和路线名称
  - 实现 `PUT /api/profile/me`，支持创建或更新当前用户目标配置
  - `PUT /api/profile/me` 支持按显式 `routeId` 绑定路线，或根据 `goalType` 自动绑定默认路线
  - 首次保存目标配置时初始化 `user_attribute`
  - 首次保存目标配置时初始化 `user_route_progress`，并尝试绑定路线的第一个章节和关卡
  - 新增 `V6__seed_default_growth_routes.sql`，预置 MVP 默认成长路线
  - 新增 `UserServiceTests` 和 `ProfileServiceTests`，覆盖当前用户更新、用户名冲突、目标配置缺失、首次保存初始化属性和路线进度、默认路线缺失等场景
  - 更新 `docs/api/mvp_api_design.md`，同步 `PATCH /users/me`、`GET /profile/me` 和 `PUT /profile/me` 的实际行为
  - 更新 `docs/database/database_design.md`，记录 `V6__seed_default_growth_routes.sql` 默认路线种子脚本
  - 已通过 `mvn test` 验证后端 22 个测试全部成功
- 完成每日记录提交流程的基础可用版本：
  - 实现 `POST /api/daily-logs` 的真实保存逻辑，支持保存表单字段和自然语言原始日志 `rawText`
  - 每日记录从 JWT 当前用户解析 `userId`，不接收前端传入的 `userId`
  - 同一用户同一天记录采用创建或更新策略，已存在时更新原记录并返回 `updated=true`
  - 对学习时长、工作时长、睡眠时长、运动时长、娱乐时长和任务完成率执行后端范围校验
  - `sourceType` 支持显式传入，也支持根据是否存在 `rawText` 自动推断
  - 当自然语言日志需要解析时调用 `LlmService.parseDailyLog`，表单字段优先，LLM 解析结果只补充缺失的基础字段
  - 自然语言解析失败时保留原始日志和表单字段，保存解析失败状态，不阻断基础记录提交
  - 补充 `DailyLogServiceTests`，覆盖新建、同日更新、字段越界和 LLM 失败降级场景
  - 更新 `docs/api/mvp_api_design.md`，同步每日记录提交接口的实际响应、校验规则、同日更新策略和 LLM 降级行为
  - 已通过 `mvn test` 验证后端 26 个测试全部成功
- 完成规则评分模块：
  - 实现 `ScoringService.calculate`，按规则计算学习成长、任务执行、精力恢复、情绪状态、娱乐控制和复盘质量六个维度
  - 按需求权重计算每日总分：学习成长 25%、任务执行 25%、精力恢复 15%、情绪状态 15%、娱乐控制 10%、复盘质量 10%
  - 按总分区间生成 S/A/B/C/D/E 评级
  - 每个维度输出可解释原因，评分完全由后端规则计算，不依赖 LLM 决定最终分数
  - 实现 `ScoringService.calculateAndSave`，支持按 `userId + logDate` 创建或更新 `daily_score`
  - 补充 `DailyScoreEntity` 访问器，便于后续结算、趋势和测试复用
  - 补充 `ScoringServiceTests`，覆盖正常输入、边界低分、缺失字段保守评分和 `daily_score` 更新保存
  - 更新 `docs/architecture/system_architecture.md`，同步 MVP 评分口径和可解释性规则
  - 本次未新增或调整 API 路径、请求字段或响应字段，`docs/api/mvp_api_design.md` 无需变更
  - 已通过 `mvn test` 验证后端 30 个测试全部成功
- 完成属性变化模块：
  - 实现 `AttributeService.calculate`，根据每日记录和评分结果计算 `focus`、`discipline`、`knowledge`、`energy`、`mood`、`execution`、`balance` 七项属性变化
  - 每项属性变化都输出可解释原因，后续写入 `attribute_change.reason_json`
  - 实现 `AttributeService.calculateAndSave`，支持更新当前用户 `user_attribute` 并保存或更新当日 `attribute_change`
  - 同一用户同一天重复计算时会先回滚旧属性变化和旧经验，再应用新变化，避免重复叠加
  - 属性值在服务层限制到 `0-100`
  - 经验值由每日总分换算，累计经验每 100 点提升 1 级，`exp` 记录当前等级内经验，`total_exp` 记录累计经验
  - 补充 `AttributeChangeEntity` 访问器，便于后续结算、趋势和测试复用
  - 补充 `AttributeServiceTests`，覆盖正常增益、低分扣减、属性和经验更新、同日重算幂等回滚
  - 更新 `docs/architecture/system_architecture.md`，同步 MVP 属性变化口径、经验等级规则和幂等重算规则
  - 本次未新增或调整 API 路径、请求字段或响应字段，`docs/api/mvp_api_design.md` 无需变更
  - 已通过 `mvn test` 验证后端 34 个测试全部成功
- 完成游戏化事件模块：
  - 实现 `GamificationService.generateEvents`，根据每日记录、规则评分和属性变化生成游戏化事件
  - MVP 已支持深度专注、复盘洞察、运动恢复、睡眠不足、晚间分心等高频 Buff / Debuff
  - MVP 已支持熬夜幽灵、短视频魅魔、焦虑雾兽、拖延史莱姆等敌人事件
  - 每次生成都会附带 `DAILY_SETTLEMENT` 基础每日结算事件
  - 实现 `GamificationService.generateAndSaveEvents`，同一天重新结算时先删除旧 `game_event` 再保存新事件，避免重复叠加
  - 补充 `GameEventEntity` 访问器，便于后续结算、趋势和测试复用
  - 扩展 `GameEventResult`，补充 `eventCode` 和 `effectJson`
  - 补充 `GamificationServiceTests`，覆盖正向 Buff、负向 Debuff、敌人事件和同日重算保存流程
  - 更新 `docs/architecture/system_architecture.md`，同步 MVP 游戏化事件触发规则和幂等重算规则
  - 本次未新增或调整 API 路径、请求字段或响应字段，`docs/api/mvp_api_design.md` 无需变更
  - 已通过 `mvn test` 验证后端 37 个测试全部成功
- 建立每日结算应用服务：
  - 新增 `settlement` 模块，包含 `SettlementController`、`SettlementService` 和 `SettlementResponse`
  - 实现 `POST /api/settlements`，复用每日记录提交逻辑，并同步编排 `dailylog -> llm parse -> scoring -> attribute -> gamification -> settlement`
  - 实现 `GET /api/settlements/by-date`，按当前登录用户和日期读取已生成结算结果
  - 结算响应同步返回评分、评级、评分原因、属性变化、属性变化原因、经验变化、游戏化事件、LLM 状态、降级反馈、明日任务占位和基础建议
  - LLM 自然语言解析失败时不阻断基础结算，仍返回规则评分、属性变化、事件和基础建议
  - 每日记录、评分、属性变化和游戏化事件都通过当前用户隔离，不接受前端传入的 `userId`
  - 调整 `DailyLogService`，新增 `submitAndReturnLog` 供结算服务复用保存和解析流程，原 `POST /api/daily-logs` 响应保持兼容
  - 补充 `SettlementServiceTests`，覆盖完整提交流程编排、LLM 失败降级响应、按日期读取已生成结算
  - 更新 `docs/api/mvp_api_design.md`，同步 `POST /settlements` 和 `GET /settlements/by-date` 的实际行为与响应结构
  - 更新 `docs/architecture/system_architecture.md`，同步当前每日结算同步编排流程和 LLM 降级规则
  - 已通过 `mvn test` 验证后端 39 个测试全部成功

## 当前目录状态

```text
LifeQuest/
├── agent.md
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/lifequest/LifeQuestApplication.java
│       │   ├── java/com/lifequest/auth/
│       │   │   ├── model/
│       │   │   └── security/
│       │   ├── java/com/lifequest/attribute/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/common/exception/
│       │   ├── java/com/lifequest/common/entity/
│       │   ├── java/com/lifequest/common/enums/
│       │   ├── java/com/lifequest/common/response/
│       │   ├── java/com/lifequest/config/
│       │   ├── java/com/lifequest/dailylog/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── repository/
│       │   │   └── service/
│       │   ├── java/com/lifequest/gamification/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/llm/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/profile/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/route/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/scoring/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/settlement/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   └── service/
│       │   ├── java/com/lifequest/trend/
│       │   ├── java/com/lifequest/user/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   ├── java/com/lifequest/weekly/
│       │   │   ├── entity/
│       │   │   └── repository/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-local.yml
│       │       └── db/migration/
│       │           ├── V1__create_user_and_profile_tables.sql
│       │           ├── V2__create_route_tables.sql
│       │           ├── V3__create_daily_log_and_score_tables.sql
│       │           ├── V4__create_attribute_and_game_event_tables.sql
│       │           ├── V5__create_llm_and_report_tables.sql
│       │           └── V6__seed_default_growth_routes.sql
│       └── test/
│           ├── java/com/lifequest/LifeQuestApplicationTests.java
│           ├── java/com/lifequest/attribute/service/AttributeServiceTests.java
│           ├── java/com/lifequest/auth/security/SecurityIntegrationTests.java
│           ├── java/com/lifequest/auth/service/AuthServiceTests.java
│           ├── java/com/lifequest/common/response/ApiResponseTests.java
│           ├── java/com/lifequest/dailylog/service/DailyLogServiceTests.java
│           ├── java/com/lifequest/gamification/service/GamificationServiceTests.java
│           ├── java/com/lifequest/profile/service/ProfileServiceTests.java
│           ├── java/com/lifequest/persistence/JpaEntityMappingTests.java
│           ├── java/com/lifequest/persistence/RepositoryContractTests.java
│           ├── java/com/lifequest/scoring/service/ScoringServiceTests.java
│           ├── java/com/lifequest/settlement/service/SettlementServiceTests.java
│           ├── java/com/lifequest/user/service/UserServiceTests.java
│           └── resources/application-test.yml
├── frontend/
│   ├── package.json
│   ├── package-lock.json
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── tsconfig.app.json
│   ├── tsconfig.node.json
│   ├── env.d.ts
│   └── src/
│       ├── App.vue
│       ├── main.ts
│       ├── router/index.ts
│       ├── styles/main.css
│       └── views/
│           ├── auth/
│           ├── daily-log/
│           ├── dashboard/
│           ├── route/
│           ├── settlement/
│           ├── trends/
│           └── weekly/
├── docs/
│   ├── current_process.md
│   ├── requirements/
│   │   └── product_requirements.md
│   ├── architecture/
│   │   └── system_architecture.md
│   ├── api/
│   │   └── mvp_api_design.md
│   └── database/
│       └── database_design.md
├── infra/
├── scripts/
└── .github/workflows/
```

## 后续 TODO

### 后端

#### 第一优先级：补齐 LLM、路线、趋势和周报

- 完成 LLM 统一封装和降级策略：
  - 实现 `parseDailyLog`、`generateDailyFeedback`、`generateTomorrowTasks`、`generateWeeklyReport`、`generateStoryNarration` 的接口契约
  - 保存 LLM prompt、response、调用状态和失败原因
  - LLM 失败、超时或返回异常结构时使用规则模板反馈和明日任务
  - LLM prompt 只传递完成任务所需的最小上下文，避免泄露不必要隐私
- 完成明日任务生成逻辑：
  - 生成主线任务、支线任务、防御任务
  - LLM 不可用时基于今日问题、评分短板和用户目标生成模板任务
  - 将任务与每日结算或 LLM 反馈元数据关联保存
- 完成成长路线逻辑：
  - 初始化默认成长路线、章节和关卡种子数据
  - 根据每日记录、关键词和任务完成情况推进用户路线进度
  - 实现成长路线查询接口，为前端路线页提供当前章节、当前关卡和下一目标
- 完成成长首页数据聚合：
  - 聚合今日评分、等级、路线、连续记录天数、今日状态和属性快照
  - 后续可接入 Redis 缓存近期成长面板，但 Redis 不能作为唯一业务数据来源
- 完成趋势统计接口：
  - 聚合每日评分趋势、属性变化趋势、学习时长、睡眠、情绪和娱乐控制趋势
  - 趋势接口返回前端可直接绘图的结构化数据，避免前端复制复杂统计规则
- 完成周报生成流程：
  - 汇总最近 7 天记录、评分、属性变化和游戏事件
  - 生成规则版周报结构，包括最高分、最低分、主要问题、成长点和下周建议
  - LLM 可用时生成个性化表达，LLM 不可用时返回规则模板周报

#### 第二优先级：工程质量和运行保障

- 完善 API 层测试：
  - 覆盖参数校验、统一响应、鉴权失败、权限隔离和核心接口成功路径
- 完善数据权限测试：
  - 验证用户不能读取、更新或聚合其他用户的每日记录、评分、属性、事件、反馈和周报
- 完善本地运行说明和配置：
  - 明确 MySQL、MongoDB、Redis 的本地启动方式和必需环境变量
  - 确认 Flyway 迁移脚本可在本地 MySQL 执行
- 持续更新文档：
  - 接口字段变化同步更新 `docs/api/mvp_api_design.md`
  - 表结构变化同步更新 `docs/database/database_design.md`
  - 核心流程变化同步更新 `docs/architecture/system_architecture.md`

### 前端

- 创建基础布局。
- 创建登录注册、成长首页、每日记录、每日结算、成长路线、趋势、周报等页面骨架。
- 引入图表组件目录和游戏化展示组件目录。

### 基础设施

- 添加本地开发用 Docker Compose。
- 添加 MySQL、MongoDB、Redis 的基础配置。
- 添加开发启动脚本。
- 添加基础 CI 工作流。

### 项目卫生

- 明确本地环境变量文件命名规则，例如 `.env.local`。
- 避免提交依赖目录、构建产物、日志文件和本地系统文件。

## 风险和注意事项

- 当前后端已初始化 Spring Boot + Maven 工程，但业务逻辑仍处于骨架阶段。
- 当前前端已初始化 Vue 3 + TypeScript 构建系统，但页面仍是最小占位视图。
- Element Plus 当前为全量引入，后续页面实现稳定后可改为按需引入以减小生产包体。
- 评分模块后续必须坚持规则计算，不得完全依赖 LLM。
- LLM 调用失败时必须有可用的降级结果。
- 用户日志、情绪和生活记录需要按用户隔离，不能公开展示。
