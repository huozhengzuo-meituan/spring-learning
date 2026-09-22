package dev.learning.tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

// 依赖根目录 Compose，命令见 examples/README.md。缺少服务时必须失败，不能伪装为通过。
@SpringBootTest
@ActiveProfiles("infra")
class InfrastructureIT {
    @Autowired TaskService tasks;
    @Autowired TaskCache cache;
    @Autowired StringRedisTemplate redis;
    @Autowired JdbcTemplate jdbc;
    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ObjectMapper json;
    @Autowired PlatformTransactionManager transactions;

    @Test void postgresOutboxKafkaAndDuplicateDeliveryWorkTogether() throws Exception {
        assertThat(jdbc.execute((java.sql.Connection connection) -> connection.getMetaData().getDatabaseProductName()))
                .isEqualTo("PostgreSQL");
        var task = tasks.create("真实基础设施链路 " + UUID.randomUUID());
        await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> assertThat(count(task.id())).isEqualTo(1));
        var payload = jdbc.queryForObject("SELECT payload FROM outbox_events WHERE task_id=?", String.class, task.id());
        // 同一条已处理事件再投递两次。后面的 marker 与它们同 key、同 partition。
        kafka.send(KafkaConfiguration.TOPIC, task.id().toString(), payload).get(15, TimeUnit.SECONDS);
        kafka.send(KafkaConfiguration.TOPIC, task.id().toString(), payload).get(15, TimeUnit.SECONDS);
        var marker = new TaskEvent(1, UUID.randomUUID(), task.id(), "TaskStatusChanged", 1, Instant.now());
        kafka.send(KafkaConfiguration.TOPIC, task.id().toString(), json.writeValueAsString(marker)).get(15, TimeUnit.SECONDS);
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id=?", Long.class, marker.eventId())).isEqualTo(1));
        assertThat(count(task.id())).isEqualTo(2);
    }

    @Test void redisStoresWithTtlAndInvalidatesOnlyAfterCommittedWrite() {
        var task = tasks.create("缓存实验 " + UUID.randomUUID());
        cache.put(task);
        assertThat(cache.find(task.id())).contains(task);
        var key = "learning:task:v1:" + task.id();
        assertThat(redis.getExpire(key)).isBetween(1L, 30L);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            tasks.transition(task.id(), TaskEntity.Status.IN_PROGRESS, task.version());
            status.setRollbackOnly();
        });
        assertThat(cache.find(task.id())).contains(task);
        assertThat(redis.hasKey(key)).isTrue();
        assertThat(tasks.get(task.id()).status()).isEqualTo(TaskEntity.Status.TODO);
        tasks.transition(task.id(), TaskEntity.Status.IN_PROGRESS, task.version());
        assertThat(redis.hasKey(key)).isFalse();
    }

    private long count(UUID taskId) {
        var values = jdbc.query("SELECT event_count FROM task_event_counts WHERE task_id=?", (rs, row) -> rs.getLong(1), taskId);
        return values.isEmpty() ? 0 : values.getFirst();
    }
}
