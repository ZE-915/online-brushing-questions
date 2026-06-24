# 在线刷题系统后端 README

本目录是在线刷题系统的后端工程，基于 Spring Boot、MyBatis-Plus、MySQL、Redis 和 RabbitMQ 实现。后端提供用户认证、题库管理、科目知识点管理、Excel 导入、组卷考试、错题本、考试历史和学习统计接口。

## 技术栈

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- MyBatis-Plus 3.5.14
- MySQL 8.x
- Redis
- RabbitMQ
- JWT
- Apache POI
- Maven Wrapper

## 目录结构

```text
backend/
  src/main/java/org/example/backend/
    common/        # 统一响应、业务异常、全局异常处理
    config/        # Web、数据源、Redis、RabbitMQ、MyBatis 配置
    controller/    # REST API 控制器
    dto/           # 请求与响应 DTO
    entity/        # 数据库实体
    mapper/        # MyBatis-Plus Mapper
    service/       # 业务服务
    util/          # JWT、用户上下文、拦截器
  src/main/resources/
    application.yml
    schema.sql
```

## 本地依赖

启动前需要准备：

- JDK 17
- MySQL，默认连接 `localhost:3308/quiz_mvp`
- Redis，默认 `localhost:6379`
- RabbitMQ，默认 `192.168.159.128:5672`，账号密码 `admin/admin`

默认配置位于 `src/main/resources/application.yml`：

```yaml
server:
  port: 8000
spring:
  datasource:
    url: jdbc:mysql://localhost:3308/quiz_mvp
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: 192.168.159.128
    port: 5672
    username: admin
    password: admin
app:
  cors:
    allowed-origin: http://localhost:5173
```

如本机服务地址不同，先修改该配置。

## 数据库初始化

创建数据库：

```sql
CREATE DATABASE quiz_mvp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行建表脚本：

```text
src/main/resources/schema.sql
```

当前 `spring.sql.init.mode` 为 `never`，启动应用时不会自动执行 SQL，建议手动初始化，避免误清生产数据。

## 启动与测试

在 `backend` 目录运行：

```sh
./mvnw spring-boot:run
```

Windows PowerShell 可运行：

```powershell
.\mvnw.cmd spring-boot:run
```

运行测试：

```sh
./mvnw test
```

健康检查：

```text
GET http://localhost:8000/api/health
```

## 接口模块

所有业务接口以 `/api` 为前缀，除 `/api/auth/**` 和 `/api/health` 外都需要 JWT。

| 模块 | 路径前缀 | 说明 |
| --- | --- | --- |
| 认证 | `/api/auth` | 注册、登录、签发 JWT |
| 用户 | `/api/user` | 个人资料、修改资料、修改密码 |
| 目录 | `/api/catalog` | 科目、知识点增删改查 |
| 题库 | `/api/questions` | 题目筛选、详情、新增、修改、删除 |
| 导入 | `/api/import` | Excel 批量导入题目 |
| 考试 | `/api/exams` | 生成试卷、提交答卷、历史记录、答题明细 |
| 错题 | `/api/errors` | 错题列表、标记、备注、删除 |
| 统计 | `/api/analytics` | 总览数据、知识点掌握度 |
| 健康检查 | `/api/health` | 服务状态 |

## 核心设计

- 认证鉴权：登录成功后返回 JWT，`JwtInterceptor` 解析 token 并把用户 ID 写入请求属性，业务层通过 `UserContext` 获取当前用户。
- 数据隔离：主要表都包含 `user_id`，查询和写入均按当前用户过滤，避免跨用户访问。
- 试卷缓存：生成试卷时将题目 ID 列表写入 Redis，key 格式为 `quiz:paper:{userId}:{paperId}`，有效期 30 分钟。
- 自动判分：单选、判断按答案比对，多选会排序后比对；简答和计算题支持用户自评。
- 错题沉淀：提交考试时自动记录答错题目，重复错误会增加 `wrong_count` 并更新最后错误时间。
- 异步统计：提交考试后发送 RabbitMQ 消息到 `quiz.stat.update` 队列，由监听器重建知识点掌握度。
- Excel 导入：使用 Apache POI 读取首个工作表，按表头解析科目、知识点、题型、题干、选项、答案、难度和解析。

## 主要数据表

- `user_account`：用户账号。
- `subject`：用户科目。
- `knowledge_point`：科目下的知识点。
- `question`：题目主体，包含题型、题干、选项 JSON、答案、解析和难度。
- `exam_record`：考试记录。
- `answer_record`：每题作答记录。
- `error_book`：错题本。
- `knowledge_point_stat`：知识点掌握度统计。

## 开发注意事项

- 新增需要登录的接口时，默认会被 `JwtInterceptor` 拦截；如需开放访问，需要在 `WebConfig` 中配置排除路径。
- 修改统一响应格式时，需要同步前端 `src/api/client.js` 的响应处理逻辑。
- RabbitMQ 不可用会影响考试提交后的统计更新流程，开发环境如暂不使用消息队列，需要调整相关 Bean 或本地启动 RabbitMQ。
- 当前部分源码中文字符串存在编码显示异常，建议后续统一以 UTF-8 检查并修复，避免运行时错误提示乱码。
