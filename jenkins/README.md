# LingoLens Jenkins 配置说明

## 基础工具

Jenkins 的 `Manage Jenkins -> Tools` 需要配置：

| 类型 | 名称 | 建议版本 |
| --- | --- | --- |
| JDK | `JDK17` | JDK 17 |
| Maven | `Maven3` | Maven 3.9.15 |

## 凭据

Jenkins 的 `Manage Jenkins -> Credentials` 需要配置：

| ID | 类型 | 用途 |
| --- | --- | --- |
| `git-ssh-auth` | SSH Username with private key | 拉取 GitHub 仓库 |
| `nexus-admin` | Username with password | 发布 Maven artifact 到 Nexus |

当前 Jenkinsfile 使用 SSH 拉取：

```text
git@github.com:hatsune-hitsuzi/LingoLens.git
```

如果你想改回 HTTPS 拉取，把 Jenkinsfile 里的 `credentialsId` 改为 `git-repo-auth`，URL 改为 `https://github.com/hatsune-hitsuzi/LingoLens.git`。

## Pipeline 参数

| 参数 | 默认值 | 含义 |
| --- | --- | --- |
| `SKIP_TESTS` | `true` | Maven 构建时跳过测试 |
| `DEPLOY_TO_NEXUS` | `false` | 执行 `mvn deploy` 发布到 Nexus |
| `DEPLOY_DOCKER` | `false` | 构建 Docker 镜像并重启应用容器 |
| `DOCKER_IMAGE_NAME` | `lingolens` | Docker 镜像名 |
| `DOCKER_HOST_PORT` | `18080` | 宿主机端口，映射到容器内 `8080` |

## Docker 部署前置条件

Jenkins Agent 是 Linux 时，需要先安装 Docker，并确保 Jenkins 运行用户有 Docker 权限：

```bash
docker version
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

如果 Jenkins 是容器化部署，需要把宿主机 Docker socket 挂进去，或者使用独立的部署 Agent。

## Docker 发布流程

第一次建议这样跑：

1. `SKIP_TESTS=true`
2. `DEPLOY_TO_NEXUS=false`
3. `DEPLOY_DOCKER=true`
4. `DOCKER_IMAGE_NAME=lingolens`
5. `DOCKER_HOST_PORT=18080`

发布成功后，容器名称为：

```text
lingolens-app
```

访问地址：

```text
http://159.75.140.18:18080
```

Jenkinsfile 会执行：

```bash
docker build --pull -t lingolens:${BUILD_NUMBER} -t lingolens:latest .
docker stop lingolens-app || true
docker rm lingolens-app || true
docker run -d --name lingolens-app --restart unless-stopped -p 18080:8080 lingolens:${BUILD_NUMBER}
```

## 常见问题

如果报错：

```text
permission denied while trying to connect to the Docker daemon socket
```

说明 Jenkins 用户没有 Docker 权限，需要执行：

```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

如果端口被占用，修改 `DOCKER_HOST_PORT`，不要使用 `8080`，因为 Jenkins 正在占用这个端口。
