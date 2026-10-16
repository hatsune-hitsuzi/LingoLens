# LingoLens

> An AI-assisted mobile language learning backend that turns real-world object scanning into multilingual word cards, memory-palace collections, and review workflows.

[中文文档](README.zh-CN.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg) ![Build](https://img.shields.io/badge/build-Maven-blue.svg) ![Docker](https://img.shields.io/badge/deploy-Docker-2496ED.svg) ![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📷 Preview

<!-- Replace the placeholder below with your uploaded demo video URL. -->
<!-- Example: https://github.com/user-attachments/assets/your-video-id -->

https://github.com/user-attachments/assets/replace-with-your-demo-video

```text
+---------------------------------------------------------------+
| [ LingoLens ] Scan -> Word Card -> Memory Palace -> Review    |
| Status: Backend Ready | API Docs: interface/ | CI/CD: Jenkins |
+---------------------------------------------------------------+
```

## 🛠️ Tech Stack

- **Core Language**: Java 17
- **Backend Framework**: Spring Boot 4, Spring WebMVC, Spring Security, Validation
- **Persistence**: PostgreSQL, MyBatis-Plus
- **Cache & Session State**: Redis
- **Object Storage**: MinIO OSS
- **AI / LLM Integration**: OkHttp-based client, JSON repair and normalization pipeline
- **Build & Deployment**: Maven, Docker, Jenkins Pipeline
- **Utility Libraries**: fastjson2, Lombok, pinyin4j, Kuromoji

## 🧭 Project Structure

```text
LingoLens/
├── src/main/java/com/hatsune/lingolens/
│   ├── LingoLensApplication.java        # Spring Boot entry point
│   ├── common/                          # Shared response, tools, exceptions, Redis keys, locks
│   ├── config/                          # Web MVC, security, async, interceptor configuration
│   ├── infrastructure/                  # External infrastructure adapters
│   │   ├── llm/                         # LLM client and prompt/response handling
│   │   └── oss/                         # MinIO upload implementation
│   └── module/                          # Business modules, grouped by domain
│       ├── user/                        # Registration, login, profile, preferences
│       ├── lens/                        # Object recognition, grounding, word-card APIs
│       ├── wordasset/                   # Multilingual word assets and enrichment
│       ├── memorypalace/                # Memory palace, saved words, image enhancement
│       ├── arscan/                      # AR scanned-word history
│       ├── review/                      # Daily review cards and progress tracking
│       ├── home/                        # Home dashboard statistics
│       ├── oss/                         # Upload API and public image hosting
│       └── admin/                       # Admin menu and user query APIs
├── src/main/resources/
│   ├── application.properties           # Main application configuration
│   ├── db/                              # SQL migration and schema resources
│   ├── static/                          # Static resources
│   └── templates/                       # Template resources
├── interface/                           # API docs and core business PUML diagrams
├── development-articles/                # Technical write-ups by feature domain
├── jenkins/                             # Jenkins setup documentation
├── scripts/                             # Local helper scripts
├── Dockerfile                           # Runtime image definition
├── Jenkinsfile                          # CI/CD pipeline
├── pom.xml                              # Maven project definition
└── README.md / README.zh-CN.md          # GitHub documentation
```

The code follows a domain-module style. Each business module usually contains `service`, `manager`, `mapper`, `domain`, and `dto` packages, keeping API orchestration, business rules, database access, and request/response objects separated.

## ✨ Core Features

- ⚡ **Lens Recognition Flow**: Accepts detected object tags or image-grounding results and maps them into stable multilingual word assets.
- 🎴 **Word Card System**: Generates reusable vocabulary cards with source/target language fields, examples, pronunciation data, and image metadata.
- 🏰 **Memory Palace**: Saves scanned words into scene-based collections and keeps AR scan history in sync.
- 🔁 **Review Workflow**: Tracks daily review progress through `review_word_progress` and exposes review-count, card-list, and batch-answer APIs.
- 🔐 **User Authentication**: Uses JWT plus Redis-backed login state so logout and expiration remain controllable.
- 🖼️ **Public Image Hosting**: Provides a single image-upload endpoint backed by MinIO and returns public URLs for shared access.
- 🚀 **Docker CI/CD**: GitHub push events trigger Jenkins, which pulls code, builds the jar, creates a Docker image, and restarts the application container.

## Core Functions and Implementation

### 1. User Login and Authentication

LingoLens keeps user identity stable with a hybrid approach:

- JWT carries the client token.
- Redis stores the authoritative token-to-user mapping.
- Interceptors load the current user into `UserContext`.
- Logout deletes the Redis token entry, making the token immediately invalid.

Related docs:

- [User Login](interface/user-login.md)
- [User Register](interface/user-register.md)
- [User Profile](interface/user-profile-info.md)
- [Technical Article: User Auth](development-articles/01-user-auth.md)

### 2. Lens Scan and Word Assets

The Lens flow separates fast object detection from stable language assets:

- The mobile side may submit detected tags from local YOLO/TFLite.
- The backend resolves tags into reusable `word_assets`.
- LLM-based enrichment can complete missing multilingual fields.
- The same word asset can be reused by scanning, memory palace, and review modules.

Related docs:

- [Lens Recognize](interface/lens-recognize.md)
- [Lens Word Detail](interface/lens-word-detail.md)
- [Word Asset Architecture](interface/word-asset-architecture.md)
- [Technical Article: Lens and Word Assets](development-articles/02-lens-word-asset.md)

### 3. Memory Palace and AR Scan Book

Saving a word is not just one insert. The backend coordinates multiple tables:

- `memory_palaces` stores scene-level collections.
- `memory_palace_words` stores words saved into each palace.
- `ar_scan_words` keeps the user's scanned-word history.
- Unique constraints and distributed locks prevent duplicate saves.

Related docs:

- [Add Word to Memory Palace](interface/memory-palace-word-add.md)
- [Memory Palace List](interface/memory-palace-list.md)
- [Memory Palace Word Page](interface/memory-palace-word-page.md)
- [Technical Article: Memory Palace and AR Scan](development-articles/03-memory-palace-ar-scan.md)

### 4. Review and Home Statistics

The review module derives card status from progress data instead of maintaining scattered state flags:

- Due cards are selected from saved memory-palace words.
- Daily progress is aggregated in `review_word_progress`.
- Batch answers update progress counters and today's completion state.
- Home statistics reuse the same review-progress logic.

Related docs:

- [Review APIs](interface/review-card-review.md)
- [Home Index](interface/home-index.md)
- [Technical Article: Review and Home](development-articles/04-review-home.md)

### 5. OSS Upload and Public Image Hosting

LingoLens provides both general uploads and public image hosting:

- `POST /lingolens/oss/upload` supports general file uploads.
- `POST /lingolens/oss/image/upload` supports public image hosting.
- Public images are stored under a public prefix and returned as accessible URLs.
- MinIO bucket policy can be ensured for the configured public prefix.

Related docs:

- [OSS Upload](interface/oss-upload.md)

### 6. CI/CD with Jenkins and Docker

The deployment flow is designed for a self-hosted Jenkins setup:

```text
Write code -> Commit -> Push to GitHub -> Webhook -> Jenkins
  -> Maven package -> Docker image -> Restart container -> Verify service
```

Related docs:

- [Jenkins Setup](jenkins/README.md)
- [CI/CD PUML - English](interface/core-business-logic/05-devops-ci-cd.en.puml)
- [CI/CD PUML - Chinese](interface/core-business-logic/05-devops-ci-cd.zh.puml)

## 🚀 Quick Start

### Prerequisites

- JDK 17
- Maven 3.9+
- PostgreSQL
- Redis
- MinIO

### Run Locally

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

### Build

```bash
./mvnw clean package -DskipTests
```

The packaged jar is generated under `target/`.

### Docker

```bash
docker build -t lingolens:latest .
docker run -d --name lingolens-app --restart unless-stopped -p 18080:8080 lingolens:latest
```

## ⚙️ Configuration

Use environment variables for secrets and deployment-specific values. Do not commit production credentials.

Common variables:

```text
LINGOLENS_DB_PASSWORD
LINGOLENS_REDIS_PASSWORD
LINGOLENS_JWT_SECRET
LINGOLENS_MINIO_ACCESS_KEY
LINGOLENS_MINIO_SECRET_KEY
LINGOLENS_LLM_AUTHORIZATION
```

Main configuration file:

```text
src/main/resources/application.properties
```

## 📚 Documentation

- [API Index](interface/README.md)
- [Mobile API Integration](interface/mobile-api-integration.md)
- [Core Business PUML](interface/core-business-logic/)
- [Development Articles](development-articles/README.md)
- [Jenkins Deployment](jenkins/README.md)

## 🤝 Contributing

Issues and pull requests are welcome.

1. Fork this repository and create a branch (`git checkout -b feature/AmazingFeature`)
2. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
3. Push to your branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## 📄 License

This project is licensed under the [MIT License](LICENSE). Copyright © 2026 [hatsune-hitsuzi](https://github.com/hatsune-hitsuzi).
