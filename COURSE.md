# 完整课程目录

48 课全部提供正文、回忆题、实验步骤、参考答案和官方资料。预计纯课文与短练习 24 小时；系统搭建、深入实验和毕业项目另计。主线建议 12–16 周，也可按证据调整。

| 课次 | 主题 | 本课成果 | 建议时长 |
| --- | --- | --- | --- |
| 01 | [把 Java 工具链跑通](lessons/0001-toolchain.html) | 用同一套工具完成编译、测试和启动，识别版本不匹配。 | 25 分钟 |
| 02 | [从浏览器请求走到 JVM](lessons/0002-request-journey.html) | 沿请求链定位网络、路由、业务和序列化错误。 | 25 分钟 |
| 03 | [用 record 和 Optional 表达边界](lessons/0003-modern-java.html) | 在 Java 21 中写清数据、不存在与失败三种状态。 | 30 分钟 |
| 04 | [读懂 Maven 与依赖管理](lessons/0004-maven-bom.html) | 从 pom 和依赖树解释一个类为何出现在运行时。 | 25 分钟 |
| 05 | [用构造注入理解 IoC](lessons/0005-constructor-injection.html) | 解释对象由谁创建，并写出不依赖 Spring 容器的业务测试。 | 25 分钟 |
| 06 | [单例 Bean 与并发请求](lessons/0006-singleton-lifecycle.html) | 发现共享可变字段造成的数据串扰，并理解初始化与销毁。 | 25 分钟 |
| 07 | [拆开 Spring Boot 自动配置](lessons/0007-auto-configuration.html) | 用条件报告解释一个自动配置为何生效或未生效。 | 25 分钟 |
| 08 | [配置、Profile 与密钥边界](lessons/0008-configuration.html) | 预测配置覆盖结果，并避免将环境差异写进业务代码。 | 25 分钟 |
| 09 | [把 REST 设计成资源合同](lessons/0009-rest-resources.html) | 为任务创建、查询和状态变化选择稳定的 HTTP 语义。 | 30 分钟 |
| 10 | [DTO 校验与可信边界](lessons/0010-dto-validation.html) | 分开结构校验、业务校验和数据库约束。 | 30 分钟 |
| 11 | [用 ProblemDetail 表达错误](lessons/0011-problem-detail.html) | 建立稳定错误合同，并保留服务器诊断能力。 | 30 分钟 |
| 12 | [JUnit、MockMvc 与测试边界](lessons/0012-testing-layers.html) | 为业务规则、HTTP合同和外部集成选择恰当测试层。 | 30 分钟 |
| 13 | [用 PostgreSQL 建立关系模型](lessons/0013-relational-model.html) | 让数据库约束独立维护数据有效性，而非只依赖服务代码。 | 35 分钟 |
| 14 | [JDBC 参数绑定与连接池](lessons/0014-jdbc-pool.html) | 写安全的参数化SQL，并解释连接池耗尽时请求为什么等待。 | 30 分钟 |
| 15 | [JPA 实体生命周期与脏检查](lessons/0015-jpa-state.html) | 区分普通对象、托管实体、SQL同步与事务提交。 | 30 分钟 |
| 16 | [用 Flyway 管理数据库演进](lessons/0016-flyway.html) | 把表结构变化做成可追踪、可重放的迁移。 | 30 分钟 |
| 17 | [用事务保护完整业务结果](lessons/0017-transaction-rollback.html) | 通过失败实验确认多次数据库修改一起成功或一起撤销。 | 30 分钟 |
| 18 | [代理、自调用、传播与隔离](lessons/0018-transaction-proxy.html) | 准确判断事务从哪里开始，遇到嵌套调用如何组合。 | 30 分钟 |
| 19 | [乐观锁与原子条件更新](lessons/0019-optimistic-concurrency.html) | 防止两个请求覆盖同一份状态，并把冲突反馈给客户端。 | 35 分钟 |
| 20 | [分页、索引与 N+1](lessons/0020-query-performance.html) | 从查询计划判断性能问题，并避免把数据库当作无限内存列表。 | 35 分钟 |
| 21 | [用 Testcontainers 获得真实数据库证据](lessons/0021-testcontainers.html) | 区分H2测试与PostgreSQL集成测试，并运行真实数据库约束实验。 | 35 分钟 |
| 22 | [认证、授权与过滤器链](lessons/0022-security-filter-chain.html) | 分清身份验证和权限判断，并对默认拒绝策略做验证。 | 30 分钟 |
| 23 | [Session、CSRF 与 CORS](lessons/0023-session-csrf-cors.html) | 用同一会话完成合法写请求，并说明CSRF与跨域的不同边界。 | 35 分钟 |
| 24 | [OAuth2、OIDC 与 JWT 资源服务器](lessons/0024-oauth-oidc-jwt.html) | 画清身份提供方、客户端和API的职责，并识别JWT验证缺口。 | 35 分钟 |
| 25 | [Redis：从数据模型到 TTL](lessons/0025-redis-models-ttl.html) | 为任务详情、成员集合和排行榜选择结构，并亲眼验证过期行为。 | 30 分钟 |
| 26 | [Spring Cache 与 cache-aside 一致性](lessons/0026-spring-cache-consistency.html) | 画出缓存与数据库并发时序，区分注解简化与一致性保证。 | 30 分钟 |
| 27 | [穿透、击穿、雪崩：把回源流量限制住](lessons/0027-cache-stampede-protection.html) | 根据故障的流量形状选择保护措施，并计算缓存失效时的数据库压力。 | 30 分钟 |
| 28 | [Redis 原子操作、Lua、锁与限流](lessons/0028-redis-lua-lock-rate-limit.html) | 用原子脚本保证计数与过期一起完成，并说明租约锁的边界。 | 30 分钟 |
| 29 | [Redis 持久化、复制与集群运维](lessons/0029-redis-operations.html) | 区分可恢复、高可用和可扩展，并写出一次可验证的恢复计划。 | 30 分钟 |
| 30 | [Kafka：日志、分区、key 与顺序](lessons/0030-kafka-partitions-order.html) | 创建双分区主题，观察同key顺序和不同消费组的独立进度。 | 30 分钟 |
| 31 | [Kafka 生产者：确认、重试与幂等](lessons/0031-kafka-producer-durability.html) | 解释 acks、ISR 与幂等生产者，并识别异步发送假成功。 | 30 分钟 |
| 32 | [消费组、offset 与重平衡](lessons/0032-consumer-groups-offsets.html) | 用消费组观察进度，并分析处理时间、提交时机和重平衡的关系。 | 30 分钟 |
| 33 | [至少一次投递与消费幂等](lessons/0033-consumer-idempotency.html) | 为数据库副作用设计可重试且只生效一次的消费事务。 | 30 分钟 |
| 34 | [Outbox：数据库与消息的一致性](lessons/0034-transactional-outbox.html) | 消除“任务已提交但事件没有保存”的双写窗口，并接受可恢复的重复投递。 | 30 分钟 |
| 35 | [Kafka 重试、DLT 与毒消息](lessons/0035-kafka-retries-dlt.html) | 让可恢复错误重试、永久坏消息可调查，并保持明确的顺序契约。 | 30 分钟 |
| 36 | [事件 schema 演进与契约测试](lessons/0036-event-schema-evolution.html) | 设计旧消费者仍能理解的新事件，并保存可运行的契约样本。 | 30 分钟 |
| 37 | [Actuator：指标、日志与 trace](lessons/0037-observability-actuator.html) | 从一次慢请求出发，用三类证据定位发生在哪一层。 | 30 分钟 |
| 38 | [SLO、压测与 JVM 诊断](lessons/0038-slo-load-jvm.html) | 制定可测服务目标，并用压测和JVM证据定位瓶颈。 | 30 分钟 |
| 39 | [超时、退避、熔断与隔离](lessons/0039-timeouts-retries-circuitbreaker.html) | 为依赖调用分配时间与重试预算，避免局部故障变成全局拥塞。 | 30 分钟 |
| 40 | [Java 虚拟线程与异步上下文](lessons/0040-virtual-threads-context.html) | 运行虚拟线程，并说明它为什么不能扩大数据库容量。 | 30 分钟 |
| 41 | [WebFlux、Reactor 与背压的边界](lessons/0041-webflux-reactor.html) | 比较MVC与WebFlux，并正确组合非阻塞流与阻塞依赖。 | 30 分钟 |
| 42 | [模块化单体、Modulith 与微服务边界](lessons/0042-modulith-service-boundaries.html) | 按业务能力划模块，并用可验证依赖规则决定是否需要拆服务。 | 30 分钟 |
| 43 | [Spring Cloud：Gateway、Config 与服务发现](lessons/0043-spring-cloud-compatibility.html) | 明确Cloud组件的职责，并用官方兼容表管理Boot与Cloud版本。 | 35 分钟 |
| 44 | [Spring Batch：分块、checkpoint 与重启](lessons/0044-spring-batch-restart.html) | 设计十万行导入任务，使失败后从可证明的检查点恢复。 | 30 分钟 |
| 45 | [Spring Integration、调度与分布式任务](lessons/0045-integration-scheduling.html) | 区分定时触发、消息管道和可靠作业执行，避免多实例重复副作用。 | 30 分钟 |
| 46 | [Docker、CI、Kubernetes 与优雅退出](lessons/0046-containers-ci-kubernetes.html) | 描述从构建到部署再到终止的完整生命周期，并验证探针语义。 | 35 分钟 |
| 47 | [全栈联调：OpenAPI、BFF 与安全契约](lessons/0047-fullstack-contract-security.html) | 把前端表单到后端事务的契约连起来，并区分CORS、CSRF与授权。 | 35 分钟 |
| 48 | [毕业项目：交付证据与故障演练](lessons/0048-capstone-evidence.html) | 交付一个可运行、可解释、可恢复的全栈任务系统，并用证据展示掌握程度。 | 35 分钟 |
