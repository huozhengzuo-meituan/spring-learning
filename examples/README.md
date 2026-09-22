# 可运行的 Spring 学习项目

在仓库根目录执行下文命令。基线 **Java 21、Spring Boot 4.1.1、Maven Wrapper**。由 Spring Boot 管理 Spring Framework、Security、Data、Kafka client、Hibernate、Jackson 等依赖版本，避免手工混用版本。这里演示生产技术的核心机制，不是可以直接上线的业务系统。

## 先运行，再阅读

```bash
java -version                  # 需要 Java 21；不要把 Java 17 编译报错当成业务代码问题
./mvnw -f examples/pom.xml test # 首次需要网络下载依赖；不需要 Docker
./mvnw -f examples/pom.xml -pl hello-api spring-boot:run
```

如果使用工作区内下载的 JDK，每个运行命令前加 `JAVA_HOME="$PWD/.tools/jdk-21/Contents/Home"`。只对这条命令生效，不改系统 Java。IDE 也必须将 Project SDK 和 Maven Runner JDK 设为 21。

另开终端：

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"title":"理解构造注入"}' http://127.0.0.1:8081/api/tasks
curl -s http://127.0.0.1:8081/api/tasks
curl -i -H 'Content-Type: application/json' \
  -d '{"title":" "}' http://127.0.0.1:8081/api/tasks
```

预期依次是 **201 + Location**、任务数组、**400 + ProblemDetail**。用返回的 `Location` 再 GET 单条任务；不存在的 UUID 返回 404。`hello-api` 的内存 Map 重启即清空，没有鉴权，列表暂未分页，这是从 HTTP 到 DI 的最小练习。

## 综合项目：先用 H2，不需要 Redis / Kafka

```bash
./mvnw -f examples/pom.xml -pl task-service spring-boot:run
```

访问 `http://127.0.0.1:8080`。默认使用内存 H2，启动时 Flyway 执行 `V1__task_and_outbox.sql`，Hibernate 只 `validate`，不自动改表。重启后 H2 数据消失。默认 Redis health 和 Kafka listener/admin 已关闭；infra 相关组件不会实例化，outbox 行保留待发。

| 账号 | 本地默认密码 | 权限 |
| --- | --- | --- |
| writer | writer-local-only | 读、创建、状态变更、监控 |
| reader | reader-local-only | 只读 |

应用只监听 `127.0.0.1`。HTTP Basic、内存账号和固定默认密码只用于本地理解认证链；可用 `LEARNING_WRITER_PASSWORD` / `LEARNING_READER_PASSWORD` 覆盖密码。生产需 HTTPS、真正的身份系统和资源所有权/租户授权。所有任务在此示例中属于共享空间，没有“我的任务”语义。

**CSRF 保持开启。** Basic 凭证可能被浏览器自动携带，不能简单因为接口是 REST 就禁用 CSRF。下列流程保留同一 session cookie，并把服务端返回的 token 带到写请求中：

```bash
TASK_COOKIE=$(mktemp)
TASK_TOKEN=$(curl -fsS -u writer:writer-local-only -c "$TASK_COOKIE" \
  http://127.0.0.1:8080/api/csrf | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
TASK_JSON=$(curl -fsS -u writer:writer-local-only -b "$TASK_COOKIE" \
  -H "X-CSRF-TOKEN: $TASK_TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"完成 Spring 第一阶段"}' http://127.0.0.1:8080/api/tasks)
printf '%s\n' "$TASK_JSON"
TASK_ID=$(printf '%s' "$TASK_JSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -u reader:reader-local-only "http://127.0.0.1:8080/api/tasks/$TASK_ID"
curl -fsS -u reader:reader-local-only 'http://127.0.0.1:8080/api/tasks?page=0&size=20'
curl -i -u writer:writer-local-only -b "$TASK_COOKIE" \
  -H "X-CSRF-TOKEN: $TASK_TOKEN" -H 'Content-Type: application/json' \
  -X PATCH -d '{"status":"IN_PROGRESS","version":0}' \
  "http://127.0.0.1:8080/api/tasks/$TASK_ID/status"
```

预期新建任务为 `TODO/version=0`；更新为 `IN_PROGRESS/version=1`。继续用 `version=1` 可改成 `DONE`。再次发相同 `version=0` 应返回 **409**；越级 `TODO→DONE` 也返回 409。去掉 CSRF header 得到 **403**；无凭证读接口得到 **401**；reader 即便携带有效 CSRF 仍不能写。

`page` 从 0 开始，`size` 为 1..100，超出返回 400。响应是稳定的 `content/page/size/totalElements/totalPages` DTO，不直接暴露 PageImpl。分页排序为更新时间倒序、UUID 作为第二排序键；大量数据的深分页应改成游标分页。

```bash
curl -fsS http://127.0.0.1:8080/actuator/health
curl -fsS -u writer:writer-local-only http://127.0.0.1:8080/actuator/metrics
curl -fsS -u writer:writer-local-only http://127.0.0.1:8080/actuator/prometheus
rm "$TASK_COOKIE"
```

## 打开 PostgreSQL、Redis、Kafka

需已有可用的 Docker Engine 和 Compose v2。Compose 固定具体镜像标签，避免 `latest` 静默升级：PostgreSQL 17.11、Redis 8.2.10、Kafka 4.1.2 KRaft。仅为教学选择的版本，不声称它们都是最新版本。应用运行在宿主机，Kafka 的 advertised listener 是 `localhost:9092`；若未来容器化应用，要新增内部 listener，不能直接照抄当前配置。

```bash
docker compose config --quiet
docker compose up -d --wait
# 停掉此前的 task-service，避免 8080 冲突，再运行：
./mvnw -f examples/pom.xml -pl task-service spring-boot:run \
  -Dspring-boot.run.profiles=infra
```

只学 PostgreSQL 时可以 `docker compose up -d --wait postgres`，启动参数换成 `-Dspring-boot.run.profiles=postgres`；不启用 Redis/Kafka。`infra` profile 自动包含 `postgres`。

重复前面的 CSRF 创建流程。几秒后：

```bash
curl -fsS -u reader:reader-local-only "http://127.0.0.1:8080/api/tasks/$TASK_ID/events-count"
docker compose exec postgres psql -U learning -d learning \
  -c 'SELECT event_id,task_id,created_at,published_at FROM outbox_events ORDER BY created_at DESC LIMIT 10;'
docker compose exec redis redis-cli --scan --pattern 'learning:task:v1:*'
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group learning-task-projection-v1
```

每个成功的新建/状态更新会在 **同一个数据库事务**中追加一条 outbox。每 2 秒 poller 取最多 50 条，收到 Kafka ACK 后标记已发。消费者先插入唯一 `event_id` 去重记录，再更新事件计数，二者在同一数据库事务中。消费者返回后，record ack 才推进 offset。默认 H2 模式 `events-count` 为 0 是预期，因为 Kafka 未启用。

### 真正的基础设施集成验证

```bash
# 保持 Compose 运行，但停止手工启动的 task-service，避免两个同组消费者干扰测试。
./mvnw -f examples/pom.xml -pl task-service -Pintegration verify
```

`InfrastructureIT` 会实际验证 PostgreSQL 产品名与迁移、任务→outbox→Kafka→消费者整条链路、同 eventId 重发两次后仅产生一次副作用、Redis 30 秒 TTL、回滚时保留缓存和提交后失效。测试通过同 partition 的后续 marker 确认前序消息已消费，避免只 sleep 一会就声称幂等成立。它会在本地 `learning` 数据库留下带随机 ID 的学习数据，不会清空数据库。

缺少 PostgreSQL/Redis/Kafka 时此命令应失败；**没有运行 `-Pintegration verify` 就不能声称基础设施链路已经验证**。默认 `test` 覆盖 H2/Flyway、事务整体回滚、状态机/版本冲突、并发重复 eventId 的数据库效果、副作用失败时去重标记回滚及重试、HTTP 校验和 401/403/CSRF 边界。H2 的结果不能代替 PostgreSQL 的隔离级别、锁行为或性能结论。

## 按源码阅读的路径

| 要理解的机制 | 入口 |
| --- | --- |
| 启动、扫描、构造注入、校验与 HTTP | `hello-api/.../HelloApplication.java` → `TaskController.java` → `TaskStore.java` → `ApiErrors.java` |
| API DTO、分页、输入边界 | `task-service/.../TaskController.java` / `TaskView.java` |
| 状态机、事务边界、JPA dirty checking、乐观锁 | `TaskEntity.java` / `TaskService.java` / `TaskRepository.java` |
| schema 演进而非应用任意改表 | `src/main/resources/db/migration/V1__task_and_outbox.sql` |
| SecurityFilterChain、认证与授权、CSRF | `SecurityConfiguration.java` / `TaskApiTest.java` |
| 查询编排、缓存穿透到数据库、TTL、提交后失效 | `TaskQueries.java` / `TaskCache.java` / `RedisTaskCache.java` / `NoTaskCache.java` |
| outbox 与重复投递 | `OutboxStore.java` → `OutboxPublisher.java` → `TaskEventConsumer.java` |
| 幂等原子性、失败后回滚 | `TaskEventProcessor.java` / `TaskServiceTest.java` / `InfrastructureIT.java` |
| 按环境配置、health/metrics | `application*.yaml` |

上表 `...` 对 hello-api 是 `src/main/java/dev/learning/hello/`，对 task-service 是 `src/main/java/dev/learning/tasks/`；测试在对应 `src/test/java/` 中。所有示例的 Java 注释解释设计原因和边界。

## 故障实验与明确边界

1. **Kafka 停机**：`docker compose stop kafka`，创建任务仍可落库，outbox 保持 pending，poller 日志告警。`docker compose start kafka` 后观察补发。`/actuator/health` 的 UP 不证明 Kafka 消费正常，需看 lag、pending 行数/最老年龄和消费者错误；本示例尚未自动采集这些业务指标。
2. **重复消息**：发送同一 outbox payload 两次，事件计数只增加一次。Kafka producer 的幂等配置不会替代消费者业务去重；数据库提交与 offset 提交之间仍有崩溃窗口。
3. **Redis 停机**：停止 Redis 后 GET 会记录 warning 并回源数据库；缓存 health 为 DOWN。缓存降级不代表数据库具备无限承载能力，生产还需要保护、容量规划和告警。
4. **缓存竞争**：提交后删除配合 30 秒 TTL 是允许短暂陈旧的 cache-aside。并发旧读回填或失效失败时可短暂读旧值。写入仍从数据库检查版本，因此旧值不能覆盖新值；这不是强一致读，也不适合余额等强一致结果。
5. **消费者毒消息**：当前失败会保留消息并持续重试，可能阻塞分区；尚未实现 DLT、人工重放工具、schema registry、业务告警。生产应定义有界重试与失败处理协议。
6. **多实例**：当前 outbox poller 为单实例学习实现。多实例可重复发布，而且创建时间不是严格的 aggregate 顺序保证；计数投影可交换，但有序状态投影要显式处理 aggregateVersion/缺口。后续练习为领取租约/`SKIP LOCKED`/CDC 与每聚合顺序设计。
7. **数据保留**：未做 outbox/processed_events 清理。去重保留期必须覆盖允许重放的时间窗口；随意删去重记录会再次执行旧副作用。
8. **可靠性范围**：单 broker、单副本、单 PostgreSQL、单 Redis 和无 TLS 只供本机实验。尚未实现备份恢复、滚动升级、限流、资源所有权、密码生命周期、审计、HTTP POST 幂等键、超大数据压测或生产部署流水线。

```bash
./mvnw -f examples/pom.xml package
java -jar examples/hello-api/target/hello-api-1.0.0-SNAPSHOT.jar
# 综合项目 jar:
java -jar examples/task-service/target/task-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=infra
# 停止基础设施且保留学习数据：
docker compose down
# 只有确定丢弃本工作区所有 PostgreSQL/Redis/Kafka 数据时才执行：
# docker compose down -v
```

## 官方依据

- [Spring Boot 构建与模块化 starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html)：Boot 4 使用 webmvc / webmvc-test / kafka / flyway 等对应 starter；默认 Jackson 3 import 是 `tools.jackson`。
- [Spring 事务](https://docs.spring.io/spring-framework/reference/data-access/transaction.html) 与 [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)：事务边界、repository 与实体持久化。
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)：浏览器隐式凭证与 CSRF 防护。
- [Spring Kafka 错误处理](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html)：重试、offset、recoverer 与 DLT。
- [Kafka 官方 Docker 文档](https://kafka.apache.org/41/getting-started/docker/)、[PostgreSQL 官方镜像](https://hub.docker.com/_/postgres)、[Redis 官方镜像](https://hub.docker.com/_/redis)：镜像与本地启动行为。
