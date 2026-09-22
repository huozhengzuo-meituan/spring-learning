# 本次交付验证

日期：2026-09-22，macOS arm64。Java 为工作区局部 Eclipse Temurin 21.0.12.1，Maven 3.9.11，Spring Boot 4.1.1。验证记录描述课程交付质量，不代表用户已掌握对应能力。

## 已通过

| 验证 | 结果与覆盖 |
| --- | --- |
| 默认构建与测试 | 两个可执行 jar 构建成功。hello-api 3 项 + task-service 13 项，共 16 项测试通过，无失败或跳过 |
| HTTP 真启动 | 创建与读取、非法输入400、匿名401、权限与缺CSRF的403、状态迁移、过期版本409、分页400 |
| 真实基础设施 | PostgreSQL 17.11、Redis 8.2.10、Kafka broker 4.1.2 启动并通过健康检查 |
| 真实集成测试 | 2 项 InfrastructureIT 通过：PG/Flyway→outbox→Kafka→消费计数；同eventId重发仍不重复副作用；Redis TTL与提交后删除、回滚后保留 |
| 消费失败回滚 | 数据库计数溢出导致副作用失败时，processed_events 去重标记也回滚；修正数据后同事件可重试成功 |
| 独立审查 | Java与课程/JavaScript均经过独立审查；修复Controller分层、多标签进度覆盖、JShell与zsh命令问题 |
| 教材结构 | 55个HTML页面、48课正文与测验、1,055个本地链接/锚点检查通过；每课有操作及答案，测验选项等长 |
| 浏览器实测 | 首页与课文排版、课程搜索、错误/正确测验反馈、标记练习后跨页计数、撤销与无控制台错误；测试练习记录已清除 |
| 独立片段 | 第3/24课在Java21 JShell修正后运行，分别得到防御性复制结果与Base64解码结果；第5/40课由审查者实跑通过 |

构建命令：`./mvnw -f examples/pom.xml verify`。真实集成命令：`./mvnw -f examples/pom.xml -pl task-service -Pintegration verify`。最后一次集成同时重新执行 task-service 的13项默认测试，另执行2项真实测试，均成功。

当前机器只有独立 `docker-compose`；公开镜像下载使用临时 `DOCKER_CONFIG` 和明确的 Colima socket，避开系统现有但缺失的凭据 helper，不修改用户认证配置。Kafka卷采用官方镜像已有写权限的 `/var/lib/kafka/data`；临时验证发现并修正了原目录权限问题。

## 尚未证明的范围

- 单节点 Compose 不代表多副本容灾、生产容量、真正高可用；未执行长时间压测。
- 未部署真实 OIDC 身份提供方、Cloud、Batch、Integration 或 Kubernetes；对应章节提供代码、实验方案与验收任务，按学习进度搭建。
- 第21课 Testcontainers 示例是待学习者加入的完整增量；本次真实基础设施测试采用 Compose，不冒称跑过 Testcontainers。
- 未覆盖所有第三方依赖故障组合；outbox单发布者、无DLT、缓存最多短期陈旧等限制见 examples/README.md。
- Windows、其他 CPU、Java25及升级后的依赖组合未验证。

## 环境收尾

按用户“保留配方、按需创建”偏好，验证结束后停止本课程 PostgreSQL、Redis、Kafka 容器及为本次启动的 Colima。镜像、命名卷和局部JDK保留供之后复用；没有常驻业务服务。课程静态预览可单独运行，不依赖 Docker。

原始运行日志位于忽略提交的 `.runtime/`，JUnit/Failsafe XML 位于各模块 `target/`。这些文件为本次本机证据，重建时会更新，不作为课程内容提交。
