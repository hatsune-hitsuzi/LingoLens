# LingoLens

> 一个面向移动端的 AI 语言学习后端：把现实世界的物体扫描，转化为多语言单词卡、记忆宫殿收藏和复习任务。

[English README](README.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg) ![Build](https://img.shields.io/badge/build-Maven-blue.svg) ![Docker](https://img.shields.io/badge/deploy-Docker-2496ED.svg) ![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📷 效果预览

<!-- 将下面的占位链接替换成你上传到 GitHub 后得到的演示视频地址。 -->
<!-- 示例：https://github.com/user-attachments/assets/your-video-id -->

https://github.com/user-attachments/assets/replace-with-your-demo-video

```text
+---------------------------------------------------------------+
| [ LingoLens ] 扫描 -> 单词卡 -> 记忆宫殿 -> 复习              |
| Status: Backend Ready | API Docs: interface/ | CI/CD: Jenkins |
+---------------------------------------------------------------+
```

## 🛠️ 技术栈

- **核心语言**: Java 17
- **后端框架**: Spring Boot 4、Spring WebMVC、Spring Security、Validation
- **数据持久化**: PostgreSQL、MyBatis-Plus
- **缓存与登录态**: Redis
- **对象存储**: MinIO OSS
- **AI / LLM 接入**: 基于 OkHttp 的客户端、JSON 修复与归一化处理链路
- **构建与部署**: Maven、Docker、Jenkins Pipeline
- **工具库**: fastjson2、Lombok、pinyin4j、Kuromoji

## 🧭 项目结构

```text
LingoLens/
├── src/main/java/com/hatsune/lingolens/
│   ├── LingoLensApplication.java        # Spring Boot 启动入口
│   ├── common/                          # 通用响应、工具类、异常、Redis Key、分布式锁
│   ├── config/                          # Web MVC、安全、异步线程池、拦截器配置
│   ├── infrastructure/                  # 外部基础设施适配层
│   │   ├── llm/                         # LLM 客户端、提示词与响应处理
│   │   └── oss/                         # MinIO 上传实现
│   └── module/                          # 按业务领域拆分的核心模块
│       ├── user/                        # 注册、登录、资料、偏好设置
│       ├── lens/                        # 物体识别、grounding、单词卡接口
│       ├── wordasset/                   # 多语言词资产与内容补全
│       ├── memorypalace/                # 记忆宫殿、收藏单词、图片增强
│       ├── arscan/                      # AR 扫描词本历史
│       ├── review/                      # 每日复习卡片与学习进度
│       ├── home/                        # 首页统计
│       ├── oss/                         # 上传接口与公开图床
│       └── admin/                       # 后台菜单与用户查询接口
├── src/main/resources/
│   ├── application.properties           # 主配置文件
│   ├── db/                              # SQL 脚本与表结构资源
│   ├── static/                          # 静态资源
│   └── templates/                       # 模板资源
├── interface/                           # 接口文档与核心业务 PUML 图
├── development-articles/                # 按功能域整理的技术文章
├── jenkins/                             # Jenkins 配置说明
├── scripts/                             # 本地辅助脚本
├── Dockerfile                           # 运行镜像定义
├── Jenkinsfile                          # CI/CD 流水线
├── pom.xml                              # Maven 项目定义
└── README.md / README.zh-CN.md          # GitHub 项目文档
```

整体代码采用按领域拆分的模块化结构。每个业务模块通常包含 `service`、`manager`、`mapper`、`domain`、`dto` 等包，用来区分接口编排、业务规则、数据库访问和请求响应对象。

## ✨ 核心特性

- ⚡ **Lens 扫描链路**: 接收端侧物体标签或图片 grounding 结果，并映射为稳定的多语言词资产。
- 🎴 **单词卡系统**: 生成可复用的词卡，包含源语言、目标语言、例句、发音和图片元数据。
- 🏰 **记忆宫殿**: 将扫描到的单词保存到场景化集合中，并同步维护 AR 扫描词本。
- 🔁 **复习流程**: 基于 `review_word_progress` 维护每日复习状态，提供数量、卡片列表和批量提交接口。
- 🔐 **用户登录鉴权**: 使用 JWT + Redis 登录态，支持可控过期和退出登录立即失效。
- 🖼️ **公开图床上传**: 提供单点图片上传接口，基于 MinIO 返回可公开访问的图片地址。
- 🚀 **Docker CI/CD**: GitHub push 触发 Jenkins，自动拉代码、打包 jar、构建 Docker 镜像并重启容器。

## 核心功能与实现

### 1. 用户登录与鉴权

LingoLens 使用 JWT 和 Redis 组合维护稳定登录态：

- JWT 作为客户端携带的 token。
- Redis 保存 token 到用户主键的真实映射。
- 拦截器读取 token 后写入 `UserContext`。
- 退出登录时删除 Redis token，让旧 token 立即失效。

相关文档：

- [用户登录](interface/user-login.md)
- [用户注册](interface/user-register.md)
- [用户资料](interface/user-profile-info.md)
- [技术文章：用户登录与鉴权](development-articles/01-user-auth.md)

### 2. Lens 扫描与词资产

Lens 链路将“快速识别”和“稳定词资产”拆开：

- 移动端可以提交本地 YOLO/TFLite 识别出的物体标签。
- 后端将标签解析为可复用的 `word_assets`。
- LLM 补全缺失的多语言字段和词卡内容。
- 同一个词资产可被扫描、记忆宫殿和复习模块复用。

相关文档：

- [Lens 场景识别](interface/lens-recognize.md)
- [Lens 单词卡详情](interface/lens-word-detail.md)
- [词资产架构](interface/word-asset-architecture.md)
- [技术文章：Lens 扫描与单词资产](development-articles/02-lens-word-asset.md)

### 3. 记忆宫殿与 AR 扫描词本

保存单词不是简单的一次 insert，后端会协同维护多个数据表：

- `memory_palaces` 保存场景集合。
- `memory_palace_words` 保存加入宫殿的单词。
- `ar_scan_words` 保存用户扫描过的词本历史。
- 唯一约束和分布式锁共同防止重复保存。

相关文档：

- [加入记忆宫殿](interface/memory-palace-word-add.md)
- [记忆宫殿列表](interface/memory-palace-list.md)
- [记忆宫殿单词分页](interface/memory-palace-word-page.md)
- [技术文章：记忆宫殿与 AR 扫描词本](development-articles/03-memory-palace-ar-scan.md)

### 4. 复习与首页统计

复习模块不维护分散的状态枚举，而是从进度数据推导当前状态：

- 待复习卡片来自用户已保存的记忆宫殿单词。
- 每日进度聚合在 `review_word_progress`。
- 批量提交复习结果后更新正确数、错误数、总次数和今日完成状态。
- 首页统计复用同一套复习进度口径。

相关文档：

- [复习接口](interface/review-card-review.md)
- [首页信息](interface/home-index.md)
- [技术文章：复习与首页统计](development-articles/04-review-home.md)

### 5. OSS 上传与公开图床

LingoLens 同时支持通用文件上传和公开图床上传：

- `POST /lingolens/oss/upload` 用于通用文件上传。
- `POST /lingolens/oss/image/upload` 用于公开图床上传。
- 公开图片会写入公共前缀，并返回可访问 URL。
- MinIO 可自动确保公共前缀拥有读取策略。

相关文档：

- [OSS 上传接口](interface/oss-upload.md)

### 6. Jenkins + Docker 自动部署

当前部署流程适合自建 Jenkins：

```text
写代码 -> 提交 -> 推送 GitHub -> Webhook -> Jenkins
  -> Maven 打包 -> Docker 镜像 -> 重启容器 -> 验证服务
```

相关文档：

- [Jenkins 配置说明](jenkins/README.md)
- [CI/CD PUML - 英文](interface/core-business-logic/05-devops-ci-cd.en.puml)
- [CI/CD PUML - 中文](interface/core-business-logic/05-devops-ci-cd.zh.puml)

## 🚀 快速开始

### 环境要求

- JDK 17
- Maven 3.9+
- PostgreSQL
- Redis
- MinIO

### 本地运行

```bash
./mvnw spring-boot:run
```

Windows：

```cmd
mvnw.cmd spring-boot:run
```

### 打包

```bash
./mvnw clean package -DskipTests
```

构建产物会生成在 `target/` 目录下。

### Docker

```bash
docker build -t lingolens:latest .
docker run -d --name lingolens-app --restart unless-stopped -p 18080:8080 lingolens:latest
```

## ⚙️ 配置说明

请使用环境变量管理密钥和部署环境差异，不要把生产凭据提交到仓库。

常见环境变量：

```text
LINGOLENS_DB_PASSWORD
LINGOLENS_REDIS_PASSWORD
LINGOLENS_JWT_SECRET
LINGOLENS_MINIO_ACCESS_KEY
LINGOLENS_MINIO_SECRET_KEY
LINGOLENS_LLM_AUTHORIZATION
```

主配置文件：

```text
src/main/resources/application.properties
```

## 📚 文档索引

- [接口文档索引](interface/README.md)
- [移动端联调记录](interface/mobile-api-integration.md)
- [核心业务 PUML](interface/core-business-logic/)
- [开发技术文章](development-articles/README.md)
- [Jenkins 部署说明](jenkins/README.md)

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request。

1. Fork 本仓库并创建分支 (`git checkout -b feature/AmazingFeature`)
2. 提交修改 (`git commit -m 'Add some AmazingFeature'`)
3. 推送到分支 (`git push origin feature/AmazingFeature`)
4. 开启一个 Pull Request

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议开源。版权所有 © 2026 [hatsune-hitsuzi](https://github.com/hatsune-hitsuzi)。
