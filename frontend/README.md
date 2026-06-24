# 在线刷题系统前端 README

本目录是在线刷题系统的前端工程，基于 Vue 3、Vite、Vue Router 和 Element Plus 构建，负责登录注册、题库管理、科目与知识点管理、组卷练习、考试记录、错题本和学习数据看板等页面。

## 技术栈

- Vue 3
- Vite 8
- Vue Router 4
- Element Plus
- Axios
- Node.js 20.19+ 或 22.12+

## 目录结构

```text
frontend/
  src/
    api/client.js          # Axios 实例、JWT 注入、统一响应处理
    router/index.js        # 前端路由与登录守卫
    components/            # 通用组件
    views/                 # 页面视图
    assets/style.css       # 全局样式
  public/                  # 静态资源
  package.json             # 脚本与依赖
  vite.config.js           # Vite 配置
```

## 核心功能

- 用户认证：登录、注册、退出，本地保存 JWT，并通过路由守卫保护业务页面。
- 题库管理：按关键词、科目、知识点、题型和难度筛选题目，支持新增、编辑、删除。
- Excel 导入：上传题目表格，支持选择已有科目和知识点，也支持由表格内容创建目录数据。
- 科目知识点：维护个人科目和知识点分类。
- 练习考试：按随机、知识点、错题模式生成试卷，支持限时答题与提交判分。
- 错题本：展示错题、错误次数、标记状态和备注。
- 历史记录：查看考试历史和答题明细。
- 数据分析：展示题目数、练习次数、错题数、平均掌握度和知识点统计。

## 本地运行

先启动后端服务，默认地址为 `http://localhost:8000`。前端接口基地址在 `src/api/client.js` 中配置为：

```js
baseURL: 'http://localhost:8000/api'
```

安装依赖：

```sh
npm install
```

启动开发服务：

```sh
npm run dev
```

默认访问地址通常为：

```text
http://localhost:5173
```

生产构建：

```sh
npm run build
```

预览构建产物：

```sh
npm run preview
```

## 前后端联调说明

- 后端 CORS 默认允许 `http://localhost:5173`，如前端端口变化，需要同步修改后端 `application.yml` 中的 `app.cors.allowed-origin`。
- 登录成功后，前端将 token 写入 `localStorage`，后续请求通过 `Authorization: Bearer <token>` 自动携带。
- 后端统一返回结构为 `{ code, message, data }`。`client.js` 会在 `code != 0` 时弹出错误并拒绝 Promise。
- 收到 401 响应时，前端会清除本地 token 并跳转到登录页。

## 常用页面路由

| 路由 | 页面 |
| --- | --- |
| `/login` | 登录 |
| `/register` | 注册 |
| `/` | 控制台 |
| `/subjects` | 科目与知识点 |
| `/questions` | 题库列表 |
| `/questions/new` | 新增题目 |
| `/import` | 批量导入 |
| `/exam-setup` | 组卷设置 |
| `/exam` | 答题页 |
| `/exam-result` | 考试结果 |
| `/errors` | 错题本 |
| `/history` | 历史记录 |
| `/analytics` | 数据分析 |
| `/profile` | 个人资料 |

## 开发注意事项

- 新增页面时需要在 `src/router/index.js` 注册路由。
- 需要登录的页面无需单独判断 token，路由守卫会统一处理。
- 新增后端接口时优先复用 `src/api/client.js` 导出的 `api` 实例，保持鉴权和错误处理一致。
- 前端当前没有单独封装业务 API 文件，页面组件直接调用 `api`，后续如果接口数量继续增长，可按模块拆分为 `src/api/auth.js`、`src/api/questions.js` 等文件。
