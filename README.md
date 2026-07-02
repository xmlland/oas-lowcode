# OAS 低代码开发平台

OAS 是一个面向通用业务系统建设的低代码开发平台，提供动态表单、代码生成、工作流、权限管理、文件存储和 AI 辅助建模能力，可用于快速搭建管理系统、流程系统、数据采集系统和企业内部应用。

## 已有功能

- 动态表单：支持单表、主子表、树表、左树右表、多对多、自定义选择等业务建模方式。
- 表单设计器：可视化配置字段、布局、校验、字典、列表、查询、按钮和权限。
- 代码生成：基于表单和元数据生成业务页面，减少重复开发。
- 工作流：支持流程设计、待办已办、流程表单、节点权限、按钮权限、退回、催办和流程轨迹。
- 系统管理：提供用户、机构、角色、菜单、字典、系统配置、消息和审计等基础能力。
- 文件存储：支持本地存储和 MinIO，对外通过统一 storage 模块接入。
- 文档协作：支持 OnlyOffice 在线文档能力。
- AI 创建表单：支持从文字、Excel、Word、PDF、图片 OCR、原型 URL 等材料中识别表单或模块，并进入草稿确认、字典确认、体检和正式保存链路。
- 管理端：基于 Vue 3、Vite 和 Ant Design Vue 的后台管理界面。

## 项目结构

```text
oas/
|-- framework-core/         后端核心框架、认证授权、动态表单、系统管理、AI 表单服务
|-- framework-addon/        可插拔增强模块
|   |-- workflow/           工作流能力
|   |-- code-gen/           代码生成扩展
|   |-- notification/       统一通知扩展
|   `-- storage/            文件存储扩展
|-- framework-oas/          应用启动层和业务扩展
|-- framework-tools/        基础工具库
|-- framework-extensions/   可选扩展模块
|-- framework-word-export/  Word 导出服务
|-- frontend-admin/         管理端
`-- docker/                 本地开发依赖环境
```

## 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 18+
- Docker Desktop 或 Docker Engine

## 快速启动

### 1. 启动基础依赖

```bash
cd docker
docker compose up -d
```

默认会启动 PostgreSQL、Redis、MinIO 和 OnlyOffice。更多端口和账号说明见 [docker/README.md](docker/README.md)。

### 2. 编译后端

```bash
mvn -pl framework-oas -am compile -DskipTests
```

完整打包：

```bash
mvn clean package -DskipTests
```

后端启动配置以 `framework-oas/src/main/resources/application-*.yml` 为准。

### 3. 启动管理端

```bash
cd frontend-admin
npm install
npm run serve
```

生产构建：

```bash
npm run build
```

## 常用验证命令

```bash
# 后端编译
mvn -pl framework-oas -am compile -DskipTests

# 管理端构建
cd frontend-admin
npm run build

# Docker 配置检查
docker compose -f docker/docker-compose.yml config --quiet
```
