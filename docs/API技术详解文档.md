# 刷题系统 - API 技术详解文档

基于 8 个 Controller 的详细分析，本文档提供每个模块的技术细节。

## 目录

1. [认证模块](#认证模块-authcontroller)
2. [用户管理](#用户管理-usercontroller)
3. [题库管理](#题库管理-questioncontroller--catalogcontroller)
4. [考试模块](#考试模块-examcontroller)
5. [错题本](#错题本-errorbookcontroller)
6. [批量导入](#批量导入-importcontroller)
7. [学情分析](#学情分析-analyticscontroller)

---

## 认证模块 (AuthController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/auth/register` | 注册新用户 |
| POST | `/api/auth/login` | 登录并返回 JWT token |

### 请求/响应 DTO

**RegisterRequest**
```json
{
  "username": "string (3-30 chars, @NotBlank)",
  "password": "string (6-80 chars, @NotBlank)",
  "email": "string (optional)"
}
```

**LoginRequest**
```json
{
  "username": "string (@NotBlank)",
  "password": "string (@NotBlank)"
}
```

**LoginResponse**
```json
{
  "token": "string (JWT token)",
  "userId": "Long",
  "username": "string"
}
```

### 业务逻辑

**注册流程：**
1. 验证用户名不重复（BizException 9001 if duplicate）
2. 使用 BCryptPasswordEncoder 加密密码
3. 插入 UserAccount 实体
4. 返回成功

**登录流程：**
1. 按用户名查询 UserAccount
2. 使用 BCrypt.matches() 验证密码（BizException 9003 if invalid）
3. 调用 JwtUtil 生成 7 天有效期的 JWT token
4. 返回 LoginResponse (token + userId + username)

### 核心操作
- 密码加密：BCryptPasswordEncoder
- Token 生成：JwtUtil（含 userId 和 username claim）
- 错误码：9001=用户名存在, 9003=密码错误

---

## 用户管理 (UserController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/user/profile` | 查看个人资料 |
| PUT | `/api/user/profile` | 更新个人资料 |
| PUT | `/api/user/password` | 修改密码 |

### 请求/响应 DTO

**ProfileResponse**
```json
{
  "id": "Long",
  "username": "string",
  "email": "string"
}
```

**UpdateProfileRequest**
```json
{
  "username": "string (optional)",
  "email": "string (optional)"
}
```

**ChangePasswordRequest**
```json
{
  "oldPassword": "string (@NotBlank)",
  "newPassword": "string (6-20 chars)"
}
```

### 业务逻辑

- 所有操作通过 UserContext.userId(request) 提取当前用户 ID
- 修改密码时：验证旧密码正确性 → 加密新密码 → 更新
- 修改用户名时：检查新用户名唯一性
- 错误码：9004=用户不存在

---

## 题库管理 (QuestionController + CatalogController)

### QuestionController 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/questions` | 列表（带筛选和作答状态） |
| GET | `/api/questions/{id}` | 单题详情 |
| POST | `/api/questions` | 创建题目 |
| PUT | `/api/questions/{id}` | 编辑题目 |
| DELETE | `/api/questions/{id}` | 删除题目 |

### QuestionListItem 响应格式

```json
{
  "id": "Long",
  "subjectId": "Long",
  "knowledgePointId": "Long",
  "type": "string (single/multiple/blank/judge/short/calculate)",
  "stem": "string (题干)",
  "optionsJson": "string (JSON array [{key, text}])",
  "answer": "string",
  "analysis": "string",
  "difficulty": "integer (1-5)",
  "answerStatus": "integer (null=未答, 0=答错, 1=答对)"
}
```

### 查询参数 (QuestionQuery)

```json
{
  "keyword": "string (题干关键字搜索)",
  "subjectId": "Long (科目筛选)",
  "knowledgePointId": "Long (知识点筛选)",
  "type": "string (题型筛选)",
  "difficulty": "integer (难度筛选)"
}
```

### 业务逻辑

1. **列表查询**：
   - 按 userId 和筛选条件查询题目
   - 批量查询 answer_record，取每个题目最新一条记录
   - 用 statusMap 映射最新的 correctStatus
   - 返回 QuestionListItem 列表

2. **创建/编辑**：
   - 验证 @NotNull 和 @NotBlank 字段
   - 难度默认值为 1（若不提供）
   - 委托给 QuestionService 处理

3. **删除**：
   - 按 userId 和 id 删除（权限隔离）

### CatalogController 端点

**科目相关：**
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/catalog/subjects` | 列表 |
| POST | `/api/catalog/subjects` | 创建 |
| PUT | `/api/catalog/subjects/{id}` | 编辑 |
| DELETE | `/api/catalog/subjects/{id}` | 删除（级联） |

**知识点相关：**
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/catalog/knowledge-points?subjectId={id}` | 列表 |
| POST | `/api/catalog/knowledge-points` | 创建 |
| PUT | `/api/catalog/knowledge-points/{id}` | 编辑 |
| DELETE | `/api/catalog/knowledge-points/{id}` | 删除 |

### Catalog DTO

**SubjectRequest / SubjectUpdateRequest**
```json
{
  "name": "string (@NotBlank)",
  "description": "string (optional)"
}
```

**KnowledgePointRequest**
```json
{
  "subjectId": "Long (@NotNull)",
  "name": "string (@NotBlank)",
  "description": "string (optional)"
}
```

**KnowledgePointUpdateRequest**
```json
{
  "name": "string (@NotBlank)",
  "description": "string (optional)"
}
```

### Catalog 业务逻辑

1. **科目操作**：
   - 创建：插入 Subject（name 在 user_id 范围内唯一）
   - 编辑：更新现有科目
   - 删除：级联删除所有知识点，然后删除科目

2. **知识点操作**：
   - 创建：指定 subjectId，name 在 subject 范围内唯一
   - 编辑：更新 name 和 description
   - 删除：若有题目关联则抛 BizException 9032（ON DELETE RESTRICT）

---

## 考试模块 (ExamController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/exams/generate` | 生成试卷 |
| POST | `/api/exams/submit` | 提交答卷 |
| GET | `/api/exams/history` | 考试历史 |
| GET | `/api/exams/{id}/detail` | 历史详情 |

### GenerateExamRequest

```json
{
  "mode": "string (random/knowledge/error)",
  "knowledgePointId": "Long (仅 knowledge 模式需要)",
  "count": "integer (1-100, 默认10)",
  "minDifficulty": "integer (1-5, optional)",
  "maxDifficulty": "integer (1-5, optional)",
  "durationMinutes": "integer"
}
```

### ExamPaper 响应

```json
{
  "paperId": "string (UUID)",
  "mode": "string",
  "durationMinutes": "integer",
  "questions": [
    {
      "id": "Long",
      "type": "string",
      "stem": "string",
      "optionsJson": "string",
      "answer": "string",
      "difficulty": "integer"
    }
  ]
}
```

### SubmitExamRequest

```json
{
  "paperId": "string",
  "mode": "string",
  "durationSeconds": "integer",
  "answers": [
    {
      "questionId": "Long",
      "answer": "string",
      "answerSeconds": "integer",
      "selfCorrect": "boolean (仅填空/简答需要)"
    }
  ]
}
```

### ExamRecord 响应

```json
{
  "id": "Long",
  "userId": "Long",
  "name": "string ('自测试卷')",
  "mode": "string",
  "totalCount": "integer",
  "correctCount": "integer",
  "durationSeconds": "integer",
  "startTime": "LocalDateTime",
  "endTime": "LocalDateTime"
}
```

### ExamDetail 响应

```json
{
  "exam": {
    "id": "Long",
    "totalCount": "integer",
    "correctCount": "integer",
    "durationSeconds": "integer"
  },
  "answers": [
    {
      "questionId": "Long",
      "stem": "string",
      "type": "string",
      "optionsJson": "string",
      "correctAnswer": "string",
      "userAnswer": "string",
      "correctStatus": "integer (0=错, 1=对, 2=待评)"
    }
  ]
}
```

### 考试业务逻辑

**出卷流程：**
1. 按 mode 选择题目：
   - random：随机抽取 N 道题（可按难度范围筛选）
   - knowledge：从知识点随机抽取 N 道题
   - error：从错题本按 wrongCount 倒序抽取
2. 过滤脏数据：选择题但无选项 → 跳过
3. 生成 paperId (UUID)
4. 存题目 ID 到 Redis（key: `quiz:paper:{userId}:{paperId}`, TTL: 30min）
5. 返回 ExamPaper

**提交流程：**
1. 从 Redis 读取 paperId 对应的题目 ID 列表
2. 从 DB 查询完整题目数据
3. 逐题批改：
   - 单选/多选/判断：字符串比对
   - 多选答案：大写 + 排序后比对
   - 填空/简答/计算：取 selfCorrect 标志
4. 创建 ExamRecord
5. 创建 AnswerRecord（逐题）
6. 添加错题到 error_book（upsert）
7. 发送 RabbitMQ 消息（更新学情统计）
8. 删除 Redis 缓存
9. 返回 ExamRecord

**批改规则：**
- correctStatus: 0=错, 1=对, 2=待评（短答/填空）

**历史查询：**
- 按 userId 查询所有 ExamRecord
- 按 createTime 倒序

**历史详情：**
- 按 examId 查询 AnswerRecord
- 批量查询 Question
- 组装返回

---

## 错题本 (ErrorBookController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/errors` | 错题本列表 |
| PUT | `/api/errors/{id}/mark` | 标记重点 |
| PUT | `/api/errors/{id}/note` | 编辑笔记 |
| DELETE | `/api/errors/{id}` | 删除错题 |

### ErrorBookItem 响应

```json
{
  "id": "Long (ErrorBook id)",
  "stem": "string (题干)",
  "type": "string (题型)",
  "optionsJson": "string (选项)",
  "answer": "string (正确答案)",
  "analysis": "string (解析)",
  "wrongCount": "integer (错误次数)",
  "marked": "boolean (是否重点)",
  "note": "string (笔记)",
  "lastWrongTime": "LocalDateTime (最后答错时间)"
}
```

### 业务逻辑

1. **查询列表**：
   - 按 userId 查 ErrorBook，按 wrongCount 倒序
   - 批量查 Question
   - 组装返回 ErrorBookItem（含题目全信息）

2. **标记重点**：
   - 按 id 更新 marked 字段

3. **编辑笔记**：
   - 按 id 更新 note 字段

4. **删除**：
   - 按 id 删除（保留 AnswerRecord 历史）

---

## 批量导入 (ImportController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/import/questions` | 导入 Excel 文件 |

### 请求参数

```
POST /api/import/questions
Content-Type: multipart/form-data

file: MultipartFile (Excel 文件)
subjectId: Long (optional, 指定目标科目)
knowledgePointId: Long (optional, 指定目标知识点)
```

### ImportResult 响应

```json
{
  "success": "integer (成功导入数)",
  "failed": "integer (失败数)",
  "errors": ["string (错误信息)"]
}
```

### Excel 格式要求

**必需列：**
- 题目 / 题干（题目内容）
- 答案（正确答案）
- 题型（single/multiple/blank/judge/short/calculate 或中文：单选/多选/填空/判断/简答/计算）

**可选列：**
- 科目（自动创建或查找）
- 知识点（自动创建或查找）
- 难度（1-5, 默认1）
- 解析（默认空字符串）
- 选项 A/B/C/D/...（用于选择题）

### 导入业务逻辑

1. **解析表头**：灵活识别列名（中文/英文），选项列按正则 `/^(选项|option)[a-zA-Z]$/i`
2. **两种模式**：
   - 外部编目：使用提供的 subjectId/knowledgePointId
   - 内嵌编目：从 Excel 列自动创建科目/知识点
3. **数据清洗**：
   - 题干：去首尾空白
   - 答案：提取字母（支持多种格式）
   - 题型：规范化（中英文转换）
   - 难度：范围 1-5，默认 1
   - 选项：收集生成 JSON [{key, text}]
4. **脏数据过滤**：选择题但无选项 → 跳过
5. **批量插入**：逐条插入 Question，失败收集错误不中断
6. **返回结果**：成功数 + 失败数 + 错误列表

---

## 学情分析 (AnalyticsController)

### 端点

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/analytics/overview` | 学情概览 |

### 响应格式

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "questionCount": "integer (总题数)",
    "examCount": "integer (做题次数)",
    "errorCount": "integer (错题数)",
    "averageMastery": "BigDecimal (平均掌握度, 2位小数)",
    "knowledgeStats": [
      {
        "knowledgePointId": "Long",
        "knowledgePointName": "string",
        "totalQuestions": "integer",
        "correctCount": "integer",
        "masteryDegree": "BigDecimal (百分制, 2位小数)",
        "lastTestTime": "LocalDateTime"
      }
    ]
  }
}
```

### 业务逻辑

1. **统计题数**：按 userId 查 question 表
2. **统计考试次数**：按 userId 查 exam_record 表
3. **统计错题数**：按 userId 查 error_book 表
4. **知识点掌握度**：
   - 查 knowledge_point_stat 表
   - 计算公式：`(correctCount / totalQuestions) * 100`
   - 保留 2 位小数
5. **平均掌握度**：所有知识点掌握度的平均值
6. **异步更新**：RabbitMQ StatMessageListener 更新 knowledge_point_stat

---

## API 响应约定

所有 API 返回统一格式：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

**状态码：**
- `code: 0` → 成功
- `code: 40X` → 业务异常（BizException）
- `code: 401` → 未认证
- `code: 403` → 无权限
- `code: 5XX` → 系统异常

---

## 认证与授权

- **认证**：JWT Bearer Token（7 天有效期）
- **授权**：多租户隔离，所有操作按 userId 过滤

---

本文档提供了系统所有 API 的技术细节，包括端点、请求/响应格式、业务逻辑和核心操作。
