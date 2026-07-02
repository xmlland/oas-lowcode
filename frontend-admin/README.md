# OAS PC 管理端

`frontend-admin` 是 OAS 的 PC 管理端，基于 Vue 3、Vite 和 Ant Design Vue 构建。它承载系统管理、表单配置、动态表单运行、工作流、AI 创建表单/模块、OA 业务页面等主要后台能力。

## 技术栈

| 技术 | 版本/说明 |
| --- | --- |
| Vue | 3.2.x |
| Vite | 6.x |
| Ant Design Vue | 3.2.x |
| Vue Router | 4.x |
| Vuex | 4.x |
| Axios | 1.x |
| ECharts / Leaflet / OnlyOffice / TinyMCE | 按功能模块按需使用 |

## 目录结构

```text
frontend-admin/
|-- public/              静态资源、运行时配置、流程设计器、编辑器资源
|   |-- env/             debug/test/prod 环境运行时配置
|   |-- editor-app/      工作流设计器静态资源
|   `-- logo/            系统 Logo
|-- src/
|   |-- api/             接口封装
|   |-- components/      公共组件
|   |-- layout/          管理端主框架、顶部菜单、侧边菜单
|   |-- lib/             请求、权限、加密、工具函数
|   |-- router/          路由与菜单入口
|   |-- store/           Vuex 状态管理
|   `-- views/           业务页面
|       |-- admin/        系统管理
|       |-- gen/          表单配置、表单设计器、AI 创建
|       |-- oa/           工作流与办公页面
|       `-- user/         登录与认证
|-- .env.debug           本地开发环境变量
|-- .env.test            测试环境变量
|-- .env.prod            生产环境变量
`-- vite.config.js       Vite 配置
```

## 快速开始

```bash
npm install
npm run serve
```

常用脚本：

```bash
npm run serve       # 本地开发，使用 debug 模式
npm run build:test  # 测试环境构建
npm run build       # 生产环境构建，等同 prod 模式
npm run build:prod  # 生产环境构建
npm run preview     # 预览构建结果
```

本地开发通常访问：

```text
http://localhost:5173/bpm/
```

如果端口被占用，以 Vite 终端输出为准。

## 后端依赖

PC 端需要配合 `framework-oas` 后端服务运行。

常用后端信息：

| 项目 | 默认值 |
| --- | --- |
| 后端服务 | `http://127.0.0.1:6019` |
| API 前缀 | `/jeeStudio/gtoa` |
| 登录账号 | `admin` |
| 登录密码 | Demo 环境可输入任意字符 |

本地代理主要通过 `.env.debug` 和 `vite.config.js` 生效。

## 运行时配置

`public/config.js` 和 `public/env/*/config.js` 用于部署后调整部分运行时参数。当前 Logo 默认使用相对路径 `/logo/logo.png`，避免生产部署时被固定到 `/bpm/logo/logo.png`。

常见配置：

| 配置 | 说明 |
| --- | --- |
| `sysTitle` | 系统标题 |
| `tdtKey` | 天地图 Key，留空时可由后端配置补充 |
| `initLng/initLat/initZoom` | 地图默认中心点和缩放 |
| `maxFileSize` | 上传文件大小限制 |
| `acceptFiles` | 允许上传的文件类型 |
| `theme` | 主题配置，包含亮暗、菜单位置、主题色 |

## 主要页面与能力

- 工作台：首页概览、常用入口、待办和业务概况。
- 系统管理：用户、机构、角色、菜单、字典、审计、系统消息等。
- 表单配置：动态表单建模、代码生成、建表/同步表、列表页和编辑页配置。
- 表单设计器：字段、布局、校验、字典、列表、查询、隐藏区和设计器配置。
- AI 创建表单：单表单材料识别、草稿、字典建议、体检、正式化预览。
- AI 创建模块：模块蓝图识别、多表单草稿生成、模块级字典确认、批量正式保存。
- 动态表单运行时：列表、查询、弹窗编辑、页面编辑、抽屉编辑、左树右表、树表。
- 工作流：待办、已办、流程中心、流程表单和流程操作。

## AI 创建模块演示路径

1. 打开“表单配置”列表页。
2. 点击“AI 创建模块”。
3. 选择输入来源：文字、Excel、Word/PDF、图片或原型 URL。
4. 点击 AI 识别模块蓝图。
5. 确认模块蓝图后生成表单草稿。
6. 汇总并确认字典建议。
7. 执行模块级体检和正式化预览。
8. 批量正式保存。

## 构建与部署注意事项

- 生产部署时需要确保 nginx 的 `try_files` 能回退到前端入口文件。
- 如果前端部署在 `/bpm/` 路径下，nginx 的 `alias` 目录应指向实际前端构建产物目录。
- 构建产物、`node_modules/`、日志文件不应提交到开源分支。
- 运行时接口地址、AI Key、MinIO、OnlyOffice、地图 Key 等敏感配置不要写死在源码中。

## 质控建议

```bash
npm run build
```

手动质控建议覆盖：

- 登录、退出、修改密码入口。
- 顶部菜单、侧边菜单、工作台折叠逻辑。
- 系统管理典型列表：字典、用户、机构、角色。
- 表单配置列表、表单设计器、动态表单运行页。
- AI 创建模块完整主链路。
