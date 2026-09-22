package dev.learning.tasks;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("infra")
public class RedisTaskCache implements TaskCache {
    private static final Logger log = LoggerFactory.getLogger(RedisTaskCache.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    public RedisTaskCache(StringRedisTemplate redis, ObjectMapper json) { this.redis = redis; this.json = json; }
    private String key(UUID id) { return "learning:task:v1:" + id; }

    public Optional<TaskView> find(UUID id) {
        try {
            var value = redis.opsForValue().get(key(id));
            return value == null ? Optional.empty() : Optional.of(json.readValue(value, TaskView.class));
        } catch (RuntimeException ex) {
            // 教学选择：缓存故障降级读库，并留下可观测的警告；不能吞掉数据库故障。
            log.warn("Redis read failed, falling back to database: {}", ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public void put(TaskView task) {
        try { redis.opsForValue().set(key(task.id()), json.writeValueAsString(task), Duration.ofSeconds(30)); }
        catch (RuntimeException ex) { log.warn("Redis write failed: {}", ex.getClass().getSimpleName()); }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidate(TaskService.Changed changed) {
        // 只在提交成功后删除。并发旧读回填仍可能短暂陈旧，TTL 是界限；这不是强一致缓存。
        try { redis.delete(key(changed.taskId())); }
        catch (RuntimeException ex) { log.warn("Redis invalidation failed: {}", ex.getClass().getSimpleName()); }
    }
}
