# Docker 开发环境说明

本目录只保留开源版本地开发所需的基础依赖环境，用于快速启动 PostgreSQL、Redis、MinIO 和 OnlyOffice。后端与前端源码建议在本机直接启动，便于调试和二次开发。

## 目录说明

| 文件/目录 | 说明 |
| --- | --- |
| `docker-compose.yml` | 基础开发依赖：PostgreSQL、Redis、MinIO、OnlyOffice |
| `.env.example` | 环境变量示例 |
| `init-db/` | PostgreSQL 容器首次启动时执行的初始化脚本 |

## 快速启动

```bash
cd docker
copy .env.example .env
docker compose up -d
```

Linux/macOS 可使用：

```bash
cd docker
cp .env.example .env
docker compose up -d
```

## 默认端口

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| PostgreSQL | `5432` | 数据库 |
| Redis | `6379` | 缓存 |
| MinIO API | `9000` | 文件对象存储接口 |
| MinIO Console | `9001` | MinIO 管理控制台 |
| OnlyOffice | `28080` | 在线文档服务 |

## 常用命令

```bash
# 启动全部基础服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 停止并清理数据卷，执行前请确认数据可以删除
docker compose down -v
```

## 连接信息

默认连接信息来自 `.env.example`，本地开发可直接使用，发布或共享环境请自行修改密码。

| 项 | 默认值 |
| --- | --- |
| PostgreSQL 用户 | `postgres` |
| PostgreSQL 密码 | `oas123456` |
| PostgreSQL 数据库 | `oas` |
| Redis 密码 | `oas123456` |
| MinIO 用户 | `minioadmin` |
| MinIO 密码 | `minioadmin` |

MinIO 启动后会自动创建 `main` 和 `thumb` 两个 bucket，并设置为可下载。

## 注意事项

- `docker compose up -d` 会挂载 `init-db/`，初始化脚本只会在 PostgreSQL 数据卷首次创建时执行。
- 如果修改了初始化脚本并希望重新导入，需要先执行 `docker compose down -v` 清理数据卷。
- 本目录不再提供后端、前端镜像构建和完整演示环境编排；开源版默认以“容器启动依赖服务，本机启动应用源码”的方式进行开发。
