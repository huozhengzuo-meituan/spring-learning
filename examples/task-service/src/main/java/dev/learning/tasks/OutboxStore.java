package dev.learning.tasks;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OutboxStore {
    private final JdbcTemplate jdbc;
    public OutboxStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Pending(UUID eventId, UUID taskId, String payload) {}

    public void append(TaskEvent event, String payload) {
        // JdbcTemplate 与 JPA 使用同一 DataSource，参与 TaskService 的同一数据库事务。
        jdbc.update("INSERT INTO outbox_events(event_id,task_id,payload,created_at) VALUES (?,?,?,?)",
                event.eventId(), event.taskId(), payload, java.sql.Timestamp.from(event.occurredAt()));
    }

    public List<Pending> pending() {
        return jdbc.query("SELECT event_id,task_id,payload FROM outbox_events WHERE published_at IS NULL ORDER BY created_at,event_id LIMIT 50",
                (rs, row) -> new Pending(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getString(3)));
    }

    @Transactional
    public void markPublished(UUID eventId) {
        jdbc.update("UPDATE outbox_events SET published_at=? WHERE event_id=? AND published_at IS NULL",
                java.sql.Timestamp.from(Instant.now()), eventId);
    }
}
