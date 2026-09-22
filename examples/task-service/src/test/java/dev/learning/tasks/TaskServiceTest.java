package dev.learning.tasks;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TaskServiceTest {
    @Autowired TaskService tasks;
    @Autowired TaskEventProcessor processor;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test void taskAndEventCommitTogether() {
        var task = tasks.create("学习事务");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM outbox_events WHERE task_id=?", Long.class, task.id())).isEqualTo(1);
        assertThat(task.version()).isZero();
    }

    @Test void rollingBackTransactionRollsBackTaskAndEvent() {
        UUID[] id = new UUID[1];
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            id[0] = tasks.create("回滚演示").id();
            status.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tasks WHERE id=?", Long.class, id[0])).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM outbox_events WHERE task_id=?", Long.class, id[0])).isZero();
    }

    @Test void transitionsCheckBusinessRuleAndClientVersion() {
        var task = tasks.create("状态机");
        assertThatThrownBy(() -> tasks.transition(task.id(), TaskEntity.Status.DONE, task.version())).isInstanceOf(TaskConflict.class);
        var started = tasks.transition(task.id(), TaskEntity.Status.IN_PROGRESS, task.version());
        assertThat(started.version()).isEqualTo(task.version() + 1);
        assertThatThrownBy(() -> tasks.transition(task.id(), TaskEntity.Status.DONE, task.version())).isInstanceOf(TaskConflict.class);
        assertThat(tasks.transition(task.id(), TaskEntity.Status.DONE, started.version()).status()).isEqualTo(TaskEntity.Status.DONE);
    }

    @Test void concurrentDuplicateDeliveryHasOneDatabaseEffect() throws Exception {
        var task = tasks.create("消息幂等");
        var event = new TaskEvent(1, UUID.randomUUID(), task.id(), "TaskCreated", 0, Instant.now());
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> consume = () -> processor.process(event);
            var outcomes = executor.invokeAll(List.of(consume, consume));
            assertThat(List.of(outcomes.get(0).get(), outcomes.get(1).get())).containsExactlyInAnyOrder(true, false);
        }
        assertThat(jdbc.queryForObject("SELECT event_count FROM task_event_counts WHERE task_id=?", Long.class, task.id())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id=?", Long.class, event.eventId())).isEqualTo(1);
    }

    @Test void failedBusinessEffectRollsBackReceiptAndAllowsRetry() {
        var task = tasks.create("副作用失败回滚");
        var event = new TaskEvent(1, UUID.randomUUID(), task.id(), "TaskCreated", 0, Instant.now());
        // 让真正的数据库 UPDATE 在去重 INSERT 之后失败，不靠 mock 模拟事务。
        jdbc.update("INSERT INTO task_event_counts(task_id,event_count) VALUES (?,?)", task.id(), Long.MAX_VALUE);
        assertThatThrownBy(() -> processor.process(event)).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM processed_events WHERE event_id=?", Long.class, event.eventId())).isZero();
        assertThat(jdbc.queryForObject("SELECT event_count FROM task_event_counts WHERE task_id=?", Long.class, task.id())).isEqualTo(Long.MAX_VALUE);
        // 修复副作用失败原因后，同 eventId 仍能重试，不能被错误的去重标记永久丢弃。
        jdbc.update("UPDATE task_event_counts SET event_count=0 WHERE task_id=?", task.id());
        assertThat(processor.process(event)).isTrue();
        assertThat(jdbc.queryForObject("SELECT event_count FROM task_event_counts WHERE task_id=?", Long.class, task.id())).isEqualTo(1);
    }

    @Test void pageSizeIsBounded() {
        assertThatThrownBy(() -> tasks.list(0, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tasks.list(-1, 20)).isInstanceOf(IllegalArgumentException.class);
    }
}
