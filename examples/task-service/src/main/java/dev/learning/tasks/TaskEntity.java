package dev.learning.tasks;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 120) private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24) private Status status;
    // Hibernate 将 version 放入 UPDATE 的 WHERE 条件；并发冲突不能靠先查后写消除。
    @Version private Long version;
    @Column(nullable = false) private Instant updatedAt;

    protected TaskEntity() {} // JPA 需要无参构造器；业务代码走下面的构造器。
    public TaskEntity(String title) {
        this.id = UUID.randomUUID();
        this.title = title.strip();
        this.status = Status.TODO;
        this.updatedAt = Instant.now();
    }

    public enum Status { TODO, IN_PROGRESS, DONE }

    public void transition(Status next) {
        boolean allowed = status == Status.TODO && next == Status.IN_PROGRESS
                || status == Status.IN_PROGRESS && next == Status.DONE;
        if (!allowed) throw new TaskConflict("状态只允许 TODO → IN_PROGRESS → DONE");
        status = next;
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public Status getStatus() { return status; }
    public Long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
}
