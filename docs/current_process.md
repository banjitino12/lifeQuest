# LifeQuest Current Process

本文档用于记录 LifeQuest 项目的当前开发进度、已完成工作和后续 TODO。每次项目取得进展后，都应同步更新本文档。

## 当前阶段

项目处于基础框架准备阶段。

当前重点是：

- 固化项目规则和协作约束
- 明确前后端分离目录结构
- 建立文档、基础设施和脚本目录
- 为后续 Spring Boot 后端和 Vue 3 前端初始化做准备

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

## 当前目录状态

```text
LifeQuest/
├── agent.md
├── README.md
├── backend/
├── frontend/
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

- 初始化 Spring Boot 项目配置。
- 创建 Maven 或 Gradle 构建文件。
- 创建应用入口类。
- 建立统一响应结构和异常处理骨架。
- 建立用户、目标、每日记录、评分、属性、游戏化、LLM 等模块的基础类结构。
- 设计 MySQL 数据表迁移脚本。

### 前端

- 初始化 Vue 3 + TypeScript 项目配置。
- 创建前端入口文件和路由配置。
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

- 当前项目尚未初始化真实前端或后端构建系统。
- 当前目录结构为骨架，尚无业务代码。
- 评分模块后续必须坚持规则计算，不得完全依赖 LLM。
- LLM 调用失败时必须有可用的降级结果。
- 用户日志、情绪和生活记录需要按用户隔离，不能公开展示。
