# LifeQuest

LifeQuest 是一个基于 LLM 的游戏化自我成长平台。项目将用户每天的学习、生活、作息、情绪和任务完成情况转化为角色成长数据，并通过每日评分、属性变化、Buff / Debuff、成长路线、剧情旁白和明日任务，为用户提供类似 RPG 的自我成长反馈体验。

项目核心目标不是把生活变成游戏，而是让用户能够更直观地看见自己的长期成长过程，并通过稳定、及时、有解释依据的反馈获得持续行动的动力。

## 核心定位

LifeQuest 是一个把现实生活变成「可记录、可反馈、可攻略」的 AI 自我成长平台。

用户每天提交结构化记录或自然语言日志后，系统会完成以下闭环：

1. 解析每日记录
2. 计算每日评分
3. 更新角色属性
4. 生成游戏化事件
5. 输出每日结算
6. 生成 LLM 个性化反馈
7. 生成明日任务
8. 展示长期成长趋势

## MVP 功能范围

第一版 MVP 重点实现完整成长闭环，暂不追求复杂社交和重度游戏系统。

MVP 包含：

- 用户注册登录
- 目标路线选择
- 每日记录提交
- 自然语言日志解析
- 每日评分计算
- 角色属性变化
- 每日结算页
- LLM 每日反馈
- 明日任务生成
- 成长趋势可视化

MVP 暂不包含：

- 好友系统
- 公会系统
- 排行榜
- 复杂装备系统
- 移动端 App
- 可穿戴设备接入

## 基础架构

项目采用前后端分离架构：

- 前端：Vue 3 + TypeScript + Element Plus / Naive UI + ECharts
- 后端：Spring Boot + Spring Security / JWT + MyBatis Plus 或 Spring Data JPA
- 数据库：MySQL 存储结构化业务数据
- 文本数据：MongoDB 存储长文本日志、LLM 原始反馈和历史报告
- 缓存：Redis 存储近期用户状态、限流信息和会话数据
- LLM：后端统一封装 LLMService，负责日志解析、反馈生成、任务生成和周报生成

## 项目结构

```text
LifeQuest/
├── backend/                 # 后端服务
│   └── src/
│       ├── main/
│       │   ├── java/com/lifequest/
│       │   │   ├── auth/           # 登录、注册、鉴权
│       │   │   ├── user/           # 用户基础信息
│       │   │   ├── profile/        # 用户目标、路线、反馈风格
│       │   │   ├── dailylog/       # 每日记录
│       │   │   ├── scoring/        # 规则评分
│       │   │   ├── attribute/      # 角色属性与属性变化
│       │   │   ├── gamification/   # 事件、敌人、Buff / Debuff
│       │   │   ├── route/          # 成长路线与章节
│       │   │   ├── llm/            # LLM 服务封装
│       │   │   ├── trend/          # 成长趋势
│       │   │   ├── weekly/         # 周报
│       │   │   ├── common/         # 通用响应、异常、工具类
│       │   │   └── config/         # 项目配置
│       │   └── resources/
│       │       └── db/migration/   # 数据库迁移脚本
│       └── test/                   # 后端测试
├── frontend/                # 前端应用
│   └── src/
│       ├── api/             # 接口请求封装
│       ├── assets/          # 静态资源
│       ├── components/      # 通用组件
│       │   ├── charts/      # 图表组件
│       │   ├── game/        # 游戏化展示组件
│       │   └── layout/      # 布局组件
│       ├── router/          # 前端路由
│       ├── stores/          # 状态管理
│       ├── styles/          # 全局样式
│       ├── types/           # TypeScript 类型定义
│       ├── utils/           # 工具函数
│       └── views/           # 页面
│           ├── auth/        # 登录注册页
│           ├── dashboard/   # 成长首页
│           ├── daily-log/   # 每日记录页
│           ├── settlement/  # 每日结算页
│           ├── route/       # 成长路线页
│           ├── trends/      # 数据趋势页
│           └── weekly/      # 周报页
├── docs/                    # 项目文档
│   ├── requirements/        # 需求文档
│   ├── architecture/        # 架构设计
│   ├── api/                 # API 文档
│   └── database/            # 数据库设计
├── infra/                   # 基础设施配置
│   ├── docker/              # Docker 配置
│   ├── mysql/               # MySQL 配置
│   ├── mongodb/             # MongoDB 配置
│   └── redis/               # Redis 配置
├── scripts/                 # 脚本
│   ├── dev/                 # 本地开发脚本
│   └── deploy/              # 部署脚本
└── .github/workflows/       # CI 工作流
```

## 后端模块说明

- auth：负责注册、登录、JWT 签发与鉴权。
- user：维护用户账号、邮箱、头像等基础信息。
- profile：维护用户成长目标、目标周期、当前路线和反馈风格。
- dailylog：接收结构化表单记录和自然语言日志。
- scoring：根据规则计算每日总分和各维度分数。
- attribute：维护专注力、自律、知识积累、精力、情绪稳定、执行力、生活平衡等角色属性。
- gamification：生成敌人、事件、Buff / Debuff 和每日结算事件。
- route：维护成长路线、章节、关卡和进度。
- llm：统一封装 LLM 调用，包括日志解析、每日反馈、明日任务、周报和剧情旁白。
- trend：汇总每日评分、属性变化、学习时长、睡眠和情绪等趋势数据。
- weekly：生成和管理周期复盘报告。

## 数据设计方向

MVP 阶段计划包含以下核心数据表或集合：

- user
- user_profile
- daily_log
- daily_score
- user_attribute
- attribute_change
- game_event
- llm_feedback

其中结构化数据优先存储在 MySQL，长文本日志、LLM 原始响应和周报内容可扩展存储到 MongoDB。

## LLM 设计原则

LifeQuest 的评分结果不直接交给 LLM 决定。

系统采用「规则评分 + LLM 解释」的方式：

- 规则模块负责稳定计算分数、评级、经验值和属性变化。
- LLM 负责解析自然语言日志、生成反馈、生成任务和输出剧情化表达。
- 当 LLM 调用失败时，系统仍应展示基础评分、属性变化、规则建议和明日任务模板。

## 项目边界

LifeQuest 不是心理咨询系统，也不是医疗建议系统。

当用户输入明显涉及严重心理危机、健康风险等内容时，系统不应给出诊断或治疗建议，而应提示用户寻求专业帮助。
