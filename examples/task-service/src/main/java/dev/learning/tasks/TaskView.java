package dev.learning.tasks;

import java.time.Instant;
import java.util.UUID;

// API DTO 与 JPA 实体分开，防止实体关系、懒加载和内部字段意外变成接口契约。
public record TaskView(UUID id, String title, TaskEntity.Status status, Long version, Instant updatedAt) {
    public static TaskView from(TaskEntity entity) {
        return new TaskView(entity.getId(), entity.getTitle(), entity.getStatus(), entity.getVersion(), entity.getUpdatedAt());
    }
}
