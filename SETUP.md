# 环境准备

## 课程版本

| 组件 | 固定基线 | 原因 |
| --- | --- | --- |
| Java | 21 LTS | 学习现代 Java、record 与虚拟线程；不启用 preview |
| Spring Boot | 4.1.1 | 本次官方稳定文档基线，Framework 与客户端版本由 BOM 管理 |
| Maven | 3.9.11 | 官方 Wrapper 3.3.4 下载，发行包 SHA-256 校验 |
| PostgreSQL / Redis / Kafka | 见根 compose.yaml 的固定 patch tag | 同一实验可重复，非宣称最新或生产可直接部署 |

版本核验日期：2026-09-22。依据：[Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html)、[Java LTS 路线图](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)、[Boot 4 迁移说明](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)。Java 25 作为升级比较内容；21 是本课程选择，不代表最新版本。Java 发行版的许可与支持政策分别查看供应商说明。

## 当前工作区

已下载并校验 Eclipse Temurin JDK 21.0.12.1 到 `.tools/jdk-21/`。它不进入 Git，不修改机器默认 Java 17。只在当前终端使用：

```bash
cd /Users/huozhengzuo/study/spring-learning
source scripts/env.sh
./mvnw -version
./mvnw -f examples/pom.xml verify
```

`java -version` 应显示 21；`./mvnw -version` 显示 Maven 3.9.11，并确认 Java home。JDK 包来自 [Adoptium 官方 API](https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=aarch64&image_type=jdk&os=mac&vendor=eclipse)，下载时已与该 API 的 SHA-256 对照。

## 另一台机器 / 全新 clone

安装 [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) 对应系统和 CPU 的 JDK，然后把 `JAVA_HOME` 指向安装目录，并让 `$JAVA_HOME/bin` 在 PATH 前面。已有系统 JDK 21 时无需使用 `scripts/env.sh`；该脚本只负责找到本仓库 `.tools/` 下的局部 JDK。

```bash
# macOS，系统已经安装 JDK 21 时
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw -version
./mvnw -f examples/pom.xml verify
```

Windows 可使用 `mvnw.cmd -f examples/pom.xml verify`。本次未验证 Windows；课程中的 curl、变量、source 按 macOS/Linux shell 编写，Windows 可在 WSL 使用。首次构建需要网络下载 Maven 与依赖，随后可复用本地缓存。

## 中间件是第二阶段

按你的偏好，不预建完整基础设施。Compose 是按需使用的配方，学到相应章节再启动；本次临时验证用的中间件会在核验后停止。

先运行两个项目的默认配置，均无需 Docker。第 13 课起可准备 Docker Desktop 或 Colima，确认 `docker info` 成功后：

```bash
docker compose up -d --wait
./mvnw -f examples/pom.xml -pl task-service -Pintegration verify
```

如果机器只有独立 Compose 命令，则把课程中的 `docker compose` 等价替换为 `docker-compose`。本机正是这种情况。若已有 Docker 凭据配置指向不存在的 `docker-credential-osxkeychain`，修复 Docker 安装；本次验证对公开镜像使用临时空配置目录，不修改个人凭据。

占用端口：hello-api 8081、task-service 8080、PostgreSQL 5432、Redis 6379、Kafka 9092；均只监听本机。启动失败先检查端口和 Docker 资源，再看 `docker compose logs`。停止本课程中间件使用 `docker compose stop`；不要在保留学习数据时使用 `down -v`。

## 课程页面

```bash
python3 -m http.server 8765 --bind 127.0.0.1
```

打开 `http://localhost:8765/index.html`，固定同一地址阅读。直接打开 file:// 也可读全文，但不同浏览器对本地文件存储的共享规则不同；希望跨课累计练习进度时优先使用 HTTP。页面不请求外部字体、CDN 或统计接口。
