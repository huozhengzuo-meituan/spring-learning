package dev.learning.tasks;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TaskEventProcessor {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    public TaskEventProcessor(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(manager);
    }

    public boolean process(TaskEvent event) {
        if (event.version() != 1 || event.eventId() == null || event.taskId() == null
                || !("TaskCreated".equals(event.type()) || "TaskStatusChanged".equals(event.type()))) {
            throw new IllegalArgumentException("不支持的任务事件");
        }
        try {
            transaction.executeWithoutResult(status -> {
                try {
                    // 唯一键是并发下的裁判。不能用 exists(eventId) 再 insert，二者间有竞态。
                    jdbc.update("INSERT INTO processed_events(event_id,processed_at) VALUES (?,?)",
                            event.eventId(), Timestamp.from(Instant.now()));
                } catch (DuplicateKeyException duplicate) {
                    // 必须离开事务并回滚后才能忽略重复，PostgreSQL 异常事务不能继续使用。
                    throw new AlreadyProcessed();
                }
                // 去重记录和业务副作用在同一事务；副作用失败时记录也回滚。
                int updated = jdbc.update("UPDATE task_event_counts SET event_count=event_count+1 WHERE task_id=?", event.taskId());
                if (updated == 0) jdbc.update("INSERT INTO task_event_counts(task_id,event_count) VALUES (?,1)", event.taskId());
            });
            return true;
        } catch (AlreadyProcessed duplicate) {
            return false;
        }
    }

    private static final class AlreadyProcessed extends RuntimeException {}
}
