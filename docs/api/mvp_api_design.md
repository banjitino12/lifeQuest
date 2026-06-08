# LifeQuest MVP 接口设计

创建日期：2026-06-08

## 1. 设计目标

本文档定义 LifeQuest MVP 阶段的后端 REST API。

MVP 接口覆盖以下闭环：

```text
注册登录 -> 目标设置 -> 每日记录 -> 规则评分 -> 属性变化 -> 每日结算 -> 明日任务 -> 趋势和周报
```

接口设计原则：

- 评分结果由后端规则计算，前端不提交最终评分。
- LLM 结果可以异步生成，基础结算接口必须在 LLM 失败时仍可用。
- 除注册登录外，所有业务接口默认需要 JWT 鉴权。
- 用户业务数据必须以后端解析出的当前用户为准，不信任前端传入的 `userId`。
- 接口字段命名使用小驼峰，数据库字段使用下划线。

## 2. 基础约定

### 2.1 Base URL

```text
/api
```

### 2.2 鉴权

受保护接口请求头：

```http
Authorization: Bearer <accessToken>
```

### 2.3 通用成功响应

```json
{
  "code": "OK",
  "message": "success",
  "data": {}
}
```

### 2.4 通用错误响应

```json
{
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "data": {
    "field": "sleepHours",
    "reason": "睡眠时长不能小于 0"
  }
}
```

### 2.5 常用错误码

| code | HTTP 状态码 | 说明 |
| --- | --- | --- |
| OK | 200 | 请求成功 |
| CREATED | 201 | 创建成功 |
| VALIDATION_ERROR | 400 | 参数校验失败 |
| UNAUTHORIZED | 401 | 未登录或 Token 无效 |
| FORBIDDEN | 403 | 无权限 |
| NOT_FOUND | 404 | 资源不存在 |
| CONFLICT | 409 | 资源冲突 |
| RATE_LIMITED | 429 | 请求过于频繁 |
| LLM_UNAVAILABLE | 503 | LLM 不可用，但基础结果可降级返回 |
| INTERNAL_ERROR | 500 | 系统异常 |

## 3. 认证接口

### 3.1 用户注册

```http
POST /api/auth/register
```

请求：

```json
{
  "username": "tiantian",
  "email": "tiantian@example.com",
  "phone": "",
  "password": "password123"
}
```

响应：

```json
{
  "code": "CREATED",
  "message": "registered",
  "data": {
    "userId": 1,
    "username": "tiantian",
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token"
  }
}
```

规则：

- `username` 必填且唯一。
- `email` 和 `phone` 至少填写一个。
- `password` 后端必须加密后存储。

### 3.2 用户登录

```http
POST /api/auth/login
```

请求：

```json
{
  "account": "tiantian@example.com",
  "password": "password123"
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "userId": 1,
    "username": "tiantian",
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "profileCompleted": false
  }
}
```

### 3.3 刷新 Token

```http
POST /api/auth/refresh
```

请求：

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-jwt-refresh-token"
  }
}
```

### 3.4 退出登录

```http
POST /api/auth/logout
```

响应：

```json
{
  "code": "OK",
  "message": "logged out",
  "data": null
}
```

## 4. 当前用户接口

### 4.1 获取当前用户信息

```http
GET /api/users/me
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "id": 1,
    "username": "tiantian",
    "email": "tiantian@example.com",
    "phone": "",
    "avatar": "",
    "createdAt": "2026-06-08T14:00:00"
  }
}
```

### 4.2 更新当前用户信息

```http
PATCH /api/users/me
```

请求：

```json
{
  "username": "life_player",
  "avatar": "https://example.com/avatar.png"
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "id": 1,
    "username": "life_player",
    "avatar": "https://example.com/avatar.png"
  }
}
```

## 5. 用户目标与偏好接口

### 5.1 获取个人目标配置

```http
GET /api/profile/me
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "goalType": "JOB_INTERVIEW",
    "currentGoal": "准备后端实习",
    "goalPeriod": "3个月",
    "weeklyPlanHours": 20,
    "currentStage": "Redis 和项目接口阶段",
    "feedbackStyle": "GAME_NARRATOR",
    "routeId": 1,
    "routeName": "后端实习路线",
    "completed": true
  }
}
```

### 5.2 创建或更新个人目标配置

```http
PUT /api/profile/me
```

请求：

```json
{
  "goalType": "JOB_INTERVIEW",
  "currentGoal": "准备后端实习",
  "goalPeriod": "3个月",
  "weeklyPlanHours": 20,
  "currentStage": "Redis 和项目接口阶段",
  "feedbackStyle": "GAME_NARRATOR",
  "routeId": 1
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "profileId": 1,
    "attributeInitialized": true,
    "routeProgressInitialized": true
  }
}
```

规则：

- 首次保存 profile 时初始化 `user_attribute`。
- 如果绑定路线，则初始化 `user_route_progress`。

## 6. 成长首页接口

### 6.1 获取成长首页面板

```http
GET /api/dashboard
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "todayScore": 82.5,
    "todayRating": "A",
    "level": 4,
    "exp": 260,
    "nextLevelExp": 400,
    "currentRoute": {
      "routeId": 1,
      "routeName": "后端实习路线",
      "chapterName": "Redis 迷宫",
      "progressPercent": 57.0
    },
    "streakDays": 6,
    "todayStatus": "已结算",
    "attributes": {
      "focus": 68,
      "discipline": 62,
      "knowledge": 74,
      "energy": 58,
      "mood": 61,
      "execution": 70,
      "balance": 55
    },
    "todaySuggestion": "今天适合做一次 Redis 知识复盘，并守住睡前 30 分钟。"
  }
}
```

说明：

- 首页可以从 Redis 读取近期状态缓存。
- 如果当天还未记录，`todayScore` 和 `todayRating` 可以为 `null`。

## 7. 每日记录接口

### 7.1 提交每日记录

```http
POST /api/daily-logs
```

请求：

```json
{
  "logDate": "2026-06-08",
  "rawText": "今天上午复习了 Redis，下午写了项目接口，晚上有点焦虑，刷视频刷了一个半小时，运动了 20 分钟。",
  "studyHours": 3.5,
  "workHours": 2,
  "sleepHours": 6.5,
  "exerciseMinutes": 20,
  "entertainmentMinutes": 90,
  "moodTag": "焦虑",
  "taskCompletionRate": 75,
  "completedContent": "复习 Redis，完成项目接口",
  "problemText": "晚上注意力下降",
  "reflectionText": "上午效率较高，晚上需要减少短视频",
  "sourceType": "MIXED"
}
```

同步响应：

```json
{
  "code": "CREATED",
  "message": "daily log submitted",
  "data": {
    "dailyLogId": 1001,
    "logDate": "2026-06-08",
    "settlementId": 1001,
    "score": {
      "dailyScore": 78.5,
      "rating": "B",
      "growthScore": 82,
      "executionScore": 75,
      "energyScore": 68,
      "moodScore": 70,
      "distractionScore": 65,
      "reflectionScore": 85
    },
    "attributeChange": {
      "focusDelta": 2,
      "disciplineDelta": 1,
      "knowledgeDelta": 5,
      "energyDelta": -1,
      "moodDelta": 0,
      "executionDelta": 3,
      "balanceDelta": -1,
      "expDelta": 56
    },
    "llmStatus": "PENDING"
  }
}
```

规则：

- 同一用户同一天默认只允许一条主记录。
- 重复提交同一天记录可返回 `CONFLICT`，或后续设计 PUT 更新接口。
- 后端同步完成保存、规则评分、属性变化和基础事件。
- LLM 反馈、旁白、明日任务可以异步生成。

### 7.2 获取每日记录详情

```http
GET /api/daily-logs/{dailyLogId}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "dailyLogId": 1001,
    "logDate": "2026-06-08",
    "studyHours": 3.5,
    "workHours": 2,
    "sleepHours": 6.5,
    "exerciseMinutes": 20,
    "entertainmentMinutes": 90,
    "moodTag": "焦虑",
    "taskCompletionRate": 75,
    "completedContent": "复习 Redis，完成项目接口",
    "problemText": "晚上注意力下降",
    "reflectionText": "上午效率较高，晚上需要减少短视频",
    "parsedData": {
      "studyTopics": ["Redis"],
      "projectWork": ["项目接口"],
      "riskEvents": ["晚间分心"]
    }
  }
}
```

### 7.3 按日期获取每日记录

```http
GET /api/daily-logs/by-date?date=2026-06-08
```

响应同 7.2。

### 7.4 获取每日记录列表

```http
GET /api/daily-logs?startDate=2026-06-01&endDate=2026-06-08&page=1&pageSize=20
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "items": [
      {
        "dailyLogId": 1001,
        "logDate": "2026-06-08",
        "dailyScore": 78.5,
        "rating": "B",
        "studyHours": 3.5,
        "sleepHours": 6.5,
        "moodTag": "焦虑"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 1
  }
}
```

## 8. 每日结算接口

### 8.1 获取每日结算

```http
GET /api/settlements/by-date?date=2026-06-08
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "dailyLogId": 1001,
    "logDate": "2026-06-08",
    "rating": "B",
    "dailyScore": 78.5,
    "expDelta": 56,
    "scoreReasons": {
      "growthScore": "学习时长达到 3.5 小时，并推进 Redis 主题。",
      "energyScore": "睡眠 6.5 小时，恢复略不足。",
      "distractionScore": "娱乐时间 90 分钟，存在晚间分心。"
    },
    "attributeChange": {
      "focusDelta": 2,
      "disciplineDelta": 1,
      "knowledgeDelta": 5,
      "energyDelta": -1,
      "moodDelta": 0,
      "executionDelta": 3,
      "balanceDelta": -1,
      "reasons": [
        "知识积累 +5：今天推进 Redis 学习和项目接口。",
        "精力 -1：睡眠时长低于理想恢复水平。"
      ]
    },
    "events": [
      {
        "eventType": "ENEMY",
        "eventName": "短视频魅魔",
        "eventLevel": 2,
        "eventDescription": "娱乐时间偏高，影响晚间专注。"
      },
      {
        "eventType": "BUFF",
        "eventName": "复盘洞察",
        "eventDescription": "完成了有效复盘。"
      }
    ],
    "llm": {
      "status": "SUCCESS",
      "feedback": "你今天推进了 Redis 迷宫，但晚间分心仍然在影响节奏。",
      "storyNarration": "Redis 迷宫的门已经打开，真正的考验是守住晚上的注意力。",
      "fallbackUsed": false
    },
    "tomorrowTasks": [
      {
        "taskType": "MAIN",
        "title": "整理 Redis 缓存三兄弟对比表",
        "generatedBy": "LLM"
      },
      {
        "taskType": "SIDE",
        "title": "运动 20 分钟",
        "generatedBy": "LLM"
      },
      {
        "taskType": "DEFENSE",
        "title": "22:30 后不刷短视频",
        "generatedBy": "LLM"
      }
    ]
  }
}
```

### 8.2 刷新 LLM 反馈

```http
POST /api/settlements/{dailyLogId}/llm-refresh
```

响应：

```json
{
  "code": "OK",
  "message": "llm generation started",
  "data": {
    "dailyLogId": 1001,
    "llmStatus": "PENDING"
  }
}
```

说明：

- 用于 LLM 失败后手动重试。
- 不重新计算规则评分。

### 8.3 查询 LLM 生成状态

```http
GET /api/settlements/{dailyLogId}/llm-status
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "dailyLogId": 1001,
    "status": "SUCCESS",
    "feedbackReady": true,
    "tomorrowTasksReady": true,
    "storyReady": true
  }
}
```

## 9. 明日任务接口

### 9.1 获取指定日期任务

```http
GET /api/tasks?date=2026-06-09
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": [
    {
      "taskId": 501,
      "taskDate": "2026-06-09",
      "taskType": "MAIN",
      "title": "整理 Redis 缓存三兄弟对比表",
      "description": "用表格对比缓存穿透、缓存击穿、缓存雪崩。",
      "status": "TODO",
      "generatedBy": "LLM"
    }
  ]
}
```

### 9.2 更新任务状态

```http
PATCH /api/tasks/{taskId}
```

请求：

```json
{
  "status": "DONE"
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "taskId": 501,
    "status": "DONE"
  }
}
```

## 10. 成长路线接口

### 10.1 获取默认成长路线列表

```http
GET /api/routes
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": [
    {
      "routeId": 1,
      "routeCode": "BACKEND_INTERN",
      "routeName": "后端实习路线",
      "goalType": "JOB_INTERVIEW",
      "description": "从 Java 基础到技术面试的成长路线。"
    }
  ]
}
```

### 10.2 获取路线详情

```http
GET /api/routes/{routeId}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "routeId": 1,
    "routeName": "后端实习路线",
    "chapters": [
      {
        "chapterId": 11,
        "chapterNo": 3,
        "chapterName": "Redis 迷宫",
        "levels": [
          {
            "levelId": 101,
            "levelNo": 1,
            "levelName": "缓存三兄弟",
            "expReward": 30
          }
        ]
      }
    ]
  }
}
```

### 10.3 获取当前用户路线进度

```http
GET /api/routes/me/progress
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "routeId": 1,
    "routeName": "后端实习路线",
    "currentChapterId": 11,
    "currentChapterName": "Redis 迷宫",
    "currentLevelId": 101,
    "currentLevelName": "缓存三兄弟",
    "progressPercent": 57.0,
    "completedLevelCount": 8,
    "nextTarget": "整理 Redis 缓存三兄弟对比表"
  }
}
```

## 11. 趋势接口

### 11.1 获取成长趋势汇总

```http
GET /api/trends/summary?startDate=2026-06-01&endDate=2026-06-08
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "scoreTrend": [
      {
        "date": "2026-06-08",
        "dailyScore": 78.5,
        "rating": "B"
      }
    ],
    "studyHoursTrend": [
      {
        "date": "2026-06-08",
        "studyHours": 3.5
      }
    ],
    "sleepTrend": [
      {
        "date": "2026-06-08",
        "sleepHours": 6.5
      }
    ],
    "moodTrend": [
      {
        "date": "2026-06-08",
        "moodTag": "焦虑",
        "moodScore": 70
      }
    ],
    "attributeTrend": [
      {
        "date": "2026-06-08",
        "focus": 68,
        "discipline": 62,
        "knowledge": 74,
        "energy": 58,
        "mood": 61,
        "execution": 70,
        "balance": 55
      }
    ],
    "eventStats": [
      {
        "eventType": "ENEMY",
        "eventName": "短视频魅魔",
        "count": 2
      }
    ]
  }
}
```

### 11.2 获取属性雷达图数据

```http
GET /api/trends/attributes/radar
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "focus": 68,
    "discipline": 62,
    "knowledge": 74,
    "energy": 58,
    "mood": 61,
    "execution": 70,
    "balance": 55
  }
}
```

## 12. 周报接口

### 12.1 生成周报

```http
POST /api/weekly-reports
```

请求：

```json
{
  "weekStartDate": "2026-06-01",
  "weekEndDate": "2026-06-07"
}
```

响应：

```json
{
  "code": "CREATED",
  "message": "weekly report generation started",
  "data": {
    "weeklyReportId": 9001,
    "status": "GENERATING"
  }
}
```

### 12.2 获取周报详情

```http
GET /api/weekly-reports/{weeklyReportId}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "weeklyReportId": 9001,
    "weekStartDate": "2026-06-01",
    "weekEndDate": "2026-06-07",
    "highestScore": 88.0,
    "lowestScore": 62.0,
    "averageScore": 76.5,
    "mainEnemy": "晚间分心",
    "growthSummary": "本周学习推进稳定，但睡眠和晚间娱乐控制波动明显。",
    "suggestionText": "下周优先保证睡眠，并把核心学习任务前置到上午。",
    "status": "GENERATED"
  }
}
```

### 12.3 获取周报列表

```http
GET /api/weekly-reports?page=1&pageSize=10
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "items": [
      {
        "weeklyReportId": 9001,
        "weekStartDate": "2026-06-01",
        "weekEndDate": "2026-06-07",
        "averageScore": 76.5,
        "mainEnemy": "晚间分心",
        "status": "GENERATED"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 1
  }
}
```

## 13. LLM 解析接口

### 13.1 预解析自然语言日志

```http
POST /api/llm/daily-log/parse-preview
```

请求：

```json
{
  "rawText": "今天上午复习了 Redis，下午写了项目接口，晚上有点焦虑，刷视频刷了一个半小时，运动了 20 分钟。"
}
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "status": "SUCCESS",
    "parsedData": {
      "studyTopics": ["Redis"],
      "projectWork": ["项目接口"],
      "mood": "焦虑",
      "entertainmentMinutes": 90,
      "exerciseMinutes": 20,
      "riskEvents": ["晚间分心"]
    }
  }
}
```

降级响应：

```json
{
  "code": "OK",
  "message": "llm fallback used",
  "data": {
    "status": "FALLBACK",
    "parsedData": {},
    "fallbackMessage": "暂时无法解析自然语言日志，请使用表单字段补充记录。"
  }
}
```

说明：

- 该接口只做预览，不保存每日记录。
- 后续提交每日记录时后端仍需要重新校验字段。

## 14. 字典接口

### 14.1 获取 MVP 枚举字典

```http
GET /api/dictionaries
```

响应：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "goalTypes": [
      {
        "value": "STUDY_EXAM",
        "label": "学习考试路线"
      },
      {
        "value": "JOB_INTERVIEW",
        "label": "求职面试路线"
      }
    ],
    "feedbackStyles": [
      {
        "value": "CALM_COACH",
        "label": "冷静教练风"
      },
      {
        "value": "GAME_NARRATOR",
        "label": "游戏系统旁白风"
      }
    ],
    "moodTags": ["开心", "平静", "焦虑", "疲惫", "低落"],
    "taskTypes": ["MAIN", "SIDE", "DEFENSE"]
  }
}
```

## 15. 参数校验规则

### 15.1 每日记录

- `logDate` 必填，格式为 `YYYY-MM-DD`。
- `studyHours`、`workHours`、`sleepHours` 不能小于 0。
- `exerciseMinutes`、`entertainmentMinutes` 不能小于 0。
- `taskCompletionRate` 范围为 0-100。
- `rawText`、`completedContent`、`problemText`、`reflectionText` 需要限制最大长度。

### 15.2 目标配置

- `goalType` 必须为合法枚举。
- `feedbackStyle` 必须为合法枚举。
- `weeklyPlanHours` 不能小于 0。
- `routeId` 必须存在且状态为可用。

### 15.3 查询时间范围

- `startDate` 不能晚于 `endDate`。
- 趋势查询 MVP 默认最多支持 90 天范围。

## 16. 权限和隐私要求

- 所有 `/api/daily-logs/**`、`/api/settlements/**`、`/api/trends/**`、`/api/weekly-reports/**` 都只能访问当前登录用户的数据。
- 后端不得使用前端传入的 `userId` 作为数据访问依据。
- 原始日志、复盘、LLM prompt 和 LLM response 不得在公共接口中暴露。
- 当前 MVP 不提供管理员跨用户查询接口。

## 17. MVP 接口优先级

### P0

优先实现完整闭环：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/profile/me`
- `GET /api/dashboard`
- `POST /api/daily-logs`
- `GET /api/settlements/by-date`
- `GET /api/tasks`
- `GET /api/trends/summary`

### P1

完善使用体验：

- `GET /api/profile/me`
- `GET /api/daily-logs/by-date`
- `GET /api/daily-logs`
- `PATCH /api/tasks/{taskId}`
- `GET /api/routes`
- `GET /api/routes/{routeId}`
- `GET /api/routes/me/progress`
- `GET /api/trends/attributes/radar`
- `POST /api/llm/daily-log/parse-preview`
- `GET /api/dictionaries`

### P2

后续完善：

- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `PATCH /api/users/me`
- `POST /api/settlements/{dailyLogId}/llm-refresh`
- `GET /api/settlements/{dailyLogId}/llm-status`
- `POST /api/weekly-reports`
- `GET /api/weekly-reports/{weeklyReportId}`
- `GET /api/weekly-reports`

