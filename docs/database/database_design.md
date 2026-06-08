# LifeQuest 数据库表结构设计

创建日期：2026-06-08

## 1. 设计目标

数据库设计服务于 LifeQuest MVP 闭环：

```text
用户 -> 目标设置 -> 每日记录 -> 规则评分 -> 属性变化 -> 游戏化事件 -> LLM 反馈 -> 趋势和周报
```

设计目标：

- 支持用户数据隔离，所有业务数据必须绑定 `user_id`。
- 支持每日记录、评分、属性、事件和反馈的可追溯。
- 支持规则评分可解释，每次分数和属性变化都能找到原因。
- 支持 LLM 调用降级，LLM 不可用时不影响基础结算数据。
- MVP 阶段以 MySQL 为主，MongoDB 和 Redis 保留清晰扩展边界。

## 2. 存储划分

### 2.1 MySQL

存储结构化核心业务数据：

- 用户账号
- 用户目标配置
- 成长路线
- 每日记录结构化字段
- 每日评分
- 用户属性
- 属性变化
- 游戏化事件
- LLM 反馈元数据
- 周报元数据

### 2.2 MongoDB

存储长文本和半结构化数据：

- 用户原始长日志
- LLM 原始 prompt
- LLM 原始 response
- 周报正文
- 长期复盘内容

MVP 初期可以先将部分文本字段放在 MySQL 中，但业务设计上应保持 MongoDB 扩展空间。

### 2.3 Redis

存储短期缓存和限流数据：

- 用户近期成长面板缓存
- LLM 调用限流计数
- 登录会话辅助信息
- Token 黑名单
- 验证码或短期防重提交标记

Redis 不作为核心业务数据唯一存储。

## 3. 命名规范

- 表名使用小写下划线：`daily_log`
- 字段名使用小写下划线：`created_at`
- 主键统一使用 `id BIGINT`
- 用户隔离字段统一使用 `user_id BIGINT`
- 时间字段优先使用：
  - `created_at`
  - `updated_at`
  - `deleted_at`，仅软删除表需要
- 状态、类型、枚举值使用字符串，便于阅读和扩展。

## 4. MySQL 表结构

### 4.1 user

用户基础账号表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 用户 ID |
| username | VARCHAR(64) | NOT NULL, UNIQUE | 用户名 |
| email | VARCHAR(128) | UNIQUE | 邮箱 |
| phone | VARCHAR(32) | UNIQUE | 手机号 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| avatar | VARCHAR(512) |  | 头像地址 |
| status | VARCHAR(32) | NOT NULL | 用户状态：ACTIVE / DISABLED |
| last_login_at | DATETIME |  | 最近登录时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_user_username(username)`
- `uk_user_email(email)`
- `uk_user_phone(phone)`

说明：

- 不存储明文密码。
- `email` 和 `phone` MVP 阶段可二选一，但字段预留。

### 4.2 user_profile

用户成长目标和偏好配置表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 配置 ID |
| user_id | BIGINT | NOT NULL, UNIQUE | 用户 ID |
| goal_type | VARCHAR(64) | NOT NULL | 目标类型 |
| current_goal | VARCHAR(255) | NOT NULL | 当前目标 |
| goal_period | VARCHAR(64) |  | 目标周期 |
| weekly_plan_hours | DECIMAL(5,2) |  | 每周计划投入时间 |
| current_stage | VARCHAR(512) |  | 当前阶段描述 |
| feedback_style | VARCHAR(64) | NOT NULL | 反馈风格 |
| route_id | BIGINT |  | 当前路线 ID |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_user_profile_user_id(user_id)`
- `idx_user_profile_route_id(route_id)`

### 4.3 growth_route

成长路线表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 路线 ID |
| route_code | VARCHAR(64) | NOT NULL, UNIQUE | 路线编码 |
| route_name | VARCHAR(128) | NOT NULL | 路线名称 |
| goal_type | VARCHAR(64) | NOT NULL | 适用目标类型 |
| description | VARCHAR(512) |  | 路线描述 |
| is_default | BOOLEAN | NOT NULL | 是否默认路线 |
| status | VARCHAR(32) | NOT NULL | ACTIVE / DISABLED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_growth_route_code(route_code)`
- `idx_growth_route_goal_type(goal_type)`

### 4.4 route_chapter

成长路线章节表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 章节 ID |
| route_id | BIGINT | NOT NULL | 路线 ID |
| chapter_no | INT | NOT NULL | 章节序号 |
| chapter_name | VARCHAR(128) | NOT NULL | 章节名称 |
| description | VARCHAR(512) |  | 章节描述 |
| target_keywords | VARCHAR(512) |  | 关键词，逗号分隔 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `idx_route_chapter_route_id(route_id)`
- `uk_route_chapter_no(route_id, chapter_no)`

### 4.5 route_level

成长路线关卡表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 关卡 ID |
| route_id | BIGINT | NOT NULL | 路线 ID |
| chapter_id | BIGINT | NOT NULL | 章节 ID |
| level_no | INT | NOT NULL | 关卡序号 |
| level_name | VARCHAR(128) | NOT NULL | 关卡名称 |
| description | VARCHAR(512) |  | 关卡描述 |
| completion_rule_json | JSON |  | 完成条件 |
| exp_reward | INT | NOT NULL | 经验奖励 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `idx_route_level_route_id(route_id)`
- `idx_route_level_chapter_id(chapter_id)`
- `uk_route_level_no(chapter_id, level_no)`

### 4.6 user_route_progress

用户路线进度表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 进度 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| route_id | BIGINT | NOT NULL | 路线 ID |
| current_chapter_id | BIGINT |  | 当前章节 ID |
| current_level_id | BIGINT |  | 当前关卡 ID |
| progress_percent | DECIMAL(5,2) | NOT NULL | 路线进度百分比 |
| completed_level_count | INT | NOT NULL | 已完成关卡数 |
| status | VARCHAR(32) | NOT NULL | IN_PROGRESS / COMPLETED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_user_route_progress(user_id, route_id)`
- `idx_user_route_progress_user_id(user_id)`

### 4.7 daily_log

每日记录表，保存结构化记录字段。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 每日记录 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| log_date | DATE | NOT NULL | 记录日期 |
| raw_text | TEXT |  | 原始自然语言日志，MVP 可暂存 |
| study_hours | DECIMAL(5,2) | DEFAULT 0 | 学习时长 |
| work_hours | DECIMAL(5,2) | DEFAULT 0 | 工作 / 项目时长 |
| sleep_hours | DECIMAL(5,2) | DEFAULT 0 | 睡眠时长 |
| exercise_minutes | INT | DEFAULT 0 | 运动分钟数 |
| entertainment_minutes | INT | DEFAULT 0 | 娱乐分钟数 |
| mood_tag | VARCHAR(64) |  | 今日情绪 |
| task_completion_rate | INT |  | 任务完成率，0-100 |
| completed_content | TEXT |  | 今日完成内容 |
| problem_text | TEXT |  | 今日问题 |
| reflection_text | TEXT |  | 今日复盘 |
| parsed_json | JSON |  | LLM 解析后的结构化补充 |
| source_type | VARCHAR(32) | NOT NULL | FORM / NATURAL_LANGUAGE / MIXED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_daily_log_user_date(user_id, log_date)`
- `idx_daily_log_user_id(user_id)`
- `idx_daily_log_log_date(log_date)`

说明：

- 同一用户同一天只允许一条主记录。
- 如果后续引入 MongoDB，`raw_text`、`completed_content`、`problem_text`、`reflection_text` 可迁移或同步到 MongoDB。

### 4.8 daily_score

每日评分表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 评分 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| daily_log_id | BIGINT | NOT NULL | 每日记录 ID |
| log_date | DATE | NOT NULL | 记录日期 |
| daily_score | DECIMAL(5,2) | NOT NULL | 每日总分 |
| growth_score | DECIMAL(5,2) | NOT NULL | 学习成长得分 |
| execution_score | DECIMAL(5,2) | NOT NULL | 任务执行得分 |
| energy_score | DECIMAL(5,2) | NOT NULL | 精力恢复得分 |
| mood_score | DECIMAL(5,2) | NOT NULL | 情绪状态得分 |
| distraction_score | DECIMAL(5,2) | NOT NULL | 娱乐控制得分 |
| reflection_score | DECIMAL(5,2) | NOT NULL | 复盘质量得分 |
| rating | VARCHAR(8) | NOT NULL | S / A / B / C / D / E |
| reason_json | JSON | NOT NULL | 各维度评分依据 |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引：

- `uk_daily_score_user_date(user_id, log_date)`
- `idx_daily_score_user_id(user_id)`
- `idx_daily_score_daily_log_id(daily_log_id)`

### 4.9 user_attribute

用户当前角色属性表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 属性 ID |
| user_id | BIGINT | NOT NULL, UNIQUE | 用户 ID |
| focus | INT | NOT NULL | 专注力，0-100 |
| discipline | INT | NOT NULL | 自律，0-100 |
| knowledge | INT | NOT NULL | 知识积累，0-100 |
| energy | INT | NOT NULL | 精力，0-100 |
| mood | INT | NOT NULL | 情绪稳定，0-100 |
| execution | INT | NOT NULL | 执行力，0-100 |
| balance | INT | NOT NULL | 生活平衡，0-100 |
| level | INT | NOT NULL | 当前等级 |
| exp | INT | NOT NULL | 当前经验 |
| total_exp | INT | NOT NULL | 累计经验 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_user_attribute_user_id(user_id)`

说明：

- 属性值必须限制在 0-100。
- MVP 可在服务层保证范围，数据库迁移中也可增加 CHECK 约束。

### 4.10 attribute_change

每日属性变化表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 属性变化 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| daily_log_id | BIGINT | NOT NULL | 每日记录 ID |
| log_date | DATE | NOT NULL | 记录日期 |
| focus_delta | INT | NOT NULL | 专注力变化 |
| discipline_delta | INT | NOT NULL | 自律变化 |
| knowledge_delta | INT | NOT NULL | 知识积累变化 |
| energy_delta | INT | NOT NULL | 精力变化 |
| mood_delta | INT | NOT NULL | 情绪稳定变化 |
| execution_delta | INT | NOT NULL | 执行力变化 |
| balance_delta | INT | NOT NULL | 生活平衡变化 |
| exp_delta | INT | NOT NULL | 经验变化 |
| reason_json | JSON | NOT NULL | 变化原因 |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引：

- `uk_attribute_change_user_date(user_id, log_date)`
- `idx_attribute_change_daily_log_id(daily_log_id)`

### 4.11 game_event

游戏化事件表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 事件 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| daily_log_id | BIGINT |  | 每日记录 ID |
| log_date | DATE |  | 记录日期 |
| event_type | VARCHAR(64) | NOT NULL | ENEMY / BUFF / DEBUFF / ACHIEVEMENT / STORY / ROUTE |
| event_code | VARCHAR(64) |  | 事件编码 |
| event_name | VARCHAR(128) | NOT NULL | 事件名称 |
| event_level | INT |  | 事件等级 |
| event_description | VARCHAR(1024) |  | 事件描述 |
| effect_json | JSON |  | 效果数据 |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引：

- `idx_game_event_user_date(user_id, log_date)`
- `idx_game_event_daily_log_id(daily_log_id)`
- `idx_game_event_type(user_id, event_type)`

### 4.12 achievement

成就称号定义表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 成就 ID |
| achievement_code | VARCHAR(64) | NOT NULL, UNIQUE | 成就编码 |
| achievement_name | VARCHAR(128) | NOT NULL | 成就名称 |
| description | VARCHAR(512) | NOT NULL | 成就描述 |
| unlock_rule_json | JSON | NOT NULL | 解锁规则 |
| bonus_json | JSON |  | 属性加成或展示效果 |
| status | VARCHAR(32) | NOT NULL | ACTIVE / DISABLED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_achievement_code(achievement_code)`

### 4.13 user_achievement

用户成就解锁表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 用户成就 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| achievement_id | BIGINT | NOT NULL | 成就 ID |
| unlocked_at | DATETIME | NOT NULL | 解锁时间 |
| display_enabled | BOOLEAN | NOT NULL | 是否展示 |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引：

- `uk_user_achievement(user_id, achievement_id)`
- `idx_user_achievement_user_id(user_id)`

### 4.14 llm_feedback

LLM 反馈记录表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 反馈 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| daily_log_id | BIGINT |  | 每日记录 ID |
| log_date | DATE |  | 记录日期 |
| feedback_type | VARCHAR(64) | NOT NULL | PARSE_LOG / DAILY_FEEDBACK / TOMORROW_TASKS / WEEKLY_REPORT / STORY |
| status | VARCHAR(32) | NOT NULL | PENDING / SUCCESS / FAILED / FALLBACK |
| prompt | TEXT |  | MVP 可暂存，后续迁移到 MongoDB |
| response | TEXT |  | MVP 可暂存，后续迁移到 MongoDB |
| fallback_response | TEXT |  | 降级内容 |
| model_name | VARCHAR(128) |  | 模型名称 |
| error_message | VARCHAR(1024) |  | 失败原因 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `idx_llm_feedback_user_date(user_id, log_date)`
- `idx_llm_feedback_daily_log_id(daily_log_id)`
- `idx_llm_feedback_type_status(feedback_type, status)`

### 4.15 tomorrow_task

明日任务表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 任务 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| source_daily_log_id | BIGINT |  | 来源每日记录 ID |
| task_date | DATE | NOT NULL | 任务日期 |
| task_type | VARCHAR(64) | NOT NULL | MAIN / SIDE / DEFENSE |
| title | VARCHAR(255) | NOT NULL | 任务标题 |
| description | VARCHAR(1024) |  | 任务描述 |
| status | VARCHAR(32) | NOT NULL | TODO / DONE / SKIPPED |
| generated_by | VARCHAR(32) | NOT NULL | LLM / RULE |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `idx_tomorrow_task_user_date(user_id, task_date)`
- `idx_tomorrow_task_source_log(source_daily_log_id)`

### 4.16 weekly_report

周报元数据表。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 周报 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| week_start_date | DATE | NOT NULL | 周开始日期 |
| week_end_date | DATE | NOT NULL | 周结束日期 |
| highest_score | DECIMAL(5,2) |  | 本周最高分 |
| lowest_score | DECIMAL(5,2) |  | 本周最低分 |
| average_score | DECIMAL(5,2) |  | 本周平均分 |
| main_enemy | VARCHAR(128) |  | 本周主要敌人 |
| growth_summary | TEXT |  | 成长摘要，MVP 可暂存 |
| suggestion_text | TEXT |  | 下周建议，MVP 可暂存 |
| status | VARCHAR(32) | NOT NULL | GENERATED / FALLBACK |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

索引：

- `uk_weekly_report_user_week(user_id, week_start_date, week_end_date)`
- `idx_weekly_report_user_id(user_id)`

## 5. 关系概览

```text
user 1 -- 1 user_profile
user 1 -- 1 user_attribute
user 1 -- N daily_log
daily_log 1 -- 1 daily_score
daily_log 1 -- 1 attribute_change
daily_log 1 -- N game_event
daily_log 1 -- N llm_feedback
daily_log 1 -- N tomorrow_task
user 1 -- N weekly_report
growth_route 1 -- N route_chapter
route_chapter 1 -- N route_level
user 1 -- N user_route_progress
achievement 1 -- N user_achievement
```

## 6. 枚举建议

### 6.1 goal_type

- `STUDY_EXAM`
- `JOB_INTERVIEW`
- `HEALTHY_LIFE`
- `GENERAL_GROWTH`
- `CUSTOM`

### 6.2 feedback_style

- `CALM_COACH`
- `GENTLE_COMPANION`
- `SHARP_SUPERVISOR`
- `GAME_NARRATOR`
- `GALGAME_CHARACTER`

### 6.3 rating

- `S`
- `A`
- `B`
- `C`
- `D`
- `E`

### 6.4 event_type

- `ENEMY`
- `BUFF`
- `DEBUFF`
- `ACHIEVEMENT`
- `STORY`
- `ROUTE`

### 6.5 feedback_type

- `PARSE_LOG`
- `DAILY_FEEDBACK`
- `TOMORROW_TASKS`
- `WEEKLY_REPORT`
- `STORY`

## 7. 隐私和权限规则

- 除 `growth_route`、`route_chapter`、`route_level`、`achievement` 等公共配置表外，业务表必须包含 `user_id`。
- 后端查询用户业务数据时，必须使用当前登录用户 ID 作为查询条件。
- 不允许前端传入任意 `user_id` 后直接查询业务数据。
- 原始日志、情绪、复盘、LLM prompt 和 response 都属于隐私数据。
- 后续如接入分享功能，必须设计单独的脱敏视图或分享表。

## 8. 迁移脚本建议

后续 MySQL 迁移脚本建议放在：

```text
backend/src/main/resources/db/migration/
```

建议命名：

```text
V1__create_user_and_profile_tables.sql
V2__create_route_tables.sql
V3__create_daily_log_and_score_tables.sql
V4__create_attribute_and_game_event_tables.sql
V5__create_llm_and_report_tables.sql
```

## 9. MVP 建表优先级

### P0

MVP 闭环必须优先创建：

- `user`
- `user_profile`
- `daily_log`
- `daily_score`
- `user_attribute`
- `attribute_change`
- `game_event`
- `llm_feedback`
- `tomorrow_task`

### P1

用于路线和周报完善：

- `growth_route`
- `route_chapter`
- `route_level`
- `user_route_progress`
- `weekly_report`

### P2

用于成就称号扩展：

- `achievement`
- `user_achievement`

