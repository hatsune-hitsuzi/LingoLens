# LingoLens Jenkins 配置说明

## Jenkins 需要准备的信息

| 配置项 | 建议值 | 备注 |
| --- | --- | --- |
| JDK | JDK 17 | 项目 `pom.xml` 使用 Java 17 |
| Maven | Maven 3.9+ | Jenkins Agent 需要能执行 `mvn` |
| Jenkins Tools JDK 名称 | `JDK17` | Jenkinsfile 通过 `tools { jdk 'JDK17' }` 引用 |
| Jenkins Tools Maven 名称 | `Maven3` | Jenkinsfile 通过 `tools { maven 'Maven3' }` 引用 |
| 代码仓库 | 待填写 | Pipeline Job 需要 Git 仓库 URL 和凭据 |
| Nexus 凭据 ID | `nexus-admin` | 类型选择 Username with password |
| Nexus 用户名 | `admin` | 只保存在 Jenkins Credentials |
| Nexus 密码 | 不写入代码库 | 只保存在 Jenkins Credentials |
| Job 类型 | Pipeline from SCM | Script Path 填 `Jenkinsfile` |

## Jenkins 插件建议

- Pipeline
- Git
- Credentials Binding

## Job 参数

| 参数 | 默认值 | 作用 |
| --- | --- | --- |
| `SKIP_TESTS` | `true` | 跳过测试，避免测试阶段依赖真实 DB/Redis |
| `DEPLOY_TO_NEXUS` | `false` | 是否执行 `mvn deploy` 发布到 Nexus |

## 凭据配置

在 Jenkins 中进入 `Manage Jenkins -> Credentials`，新增：

| ID | 类型 | 用途 |
| --- | --- | --- |
| `nexus-admin` | Username with password | 发布 Maven artifact 到 Nexus |

`pom.xml` 中的 server id 已经是：

- `nexus-releases`
- `nexus-snapshots`

Jenkinsfile 会临时生成 `settings-ci.xml`，把 `nexus-admin` 注入到这两个 server id。

## Maven 仓库注意点

如果 Jenkins 的 Maven 全局 `settings.xml` 配了旧的 HTTP 阿里云仓库：

```xml
http://maven.aliyun.com/nexus/content/groups/public/
```

建议改成 HTTPS：

```xml
https://maven.aliyun.com/repository/public
```

或者直接使用 Maven Central，避免 Spring Boot 父 POM 拉取失败。

## 当前 Pipeline 做什么

1. 使用 `checkout scm` 拉取 Jenkins Pipeline Job 绑定的 Git 仓库代码。
2. 执行 `mvn -B -U clean package -DskipTests`。
3. 归档 `target/*.jar`。
4. 如果勾选 `DEPLOY_TO_NEXUS`，执行 `mvn deploy -Dspring-boot.repackage.skip=true` 发布 Maven 快照 artifact。

说明：Nexus deploy 阶段默认跳过 Spring Boot repackaging，发布标准 Maven jar；可运行 fat jar 仍会在 Build 阶段生成并归档。

## 还需要你给我的信息

如果要我直接进 Jenkins 帮你建 Job，还需要：

1. Jenkins 地址。
2. Jenkins 用户名和 API Token 或临时管理员密码。
3. Git 仓库 URL 和拉代码凭据。
4. Jenkins Agent 是 Linux 还是 Windows。
5. 是否还要部署到应用服务器；如果要，需要服务器 SSH 地址、用户、部署目录、启动方式。
