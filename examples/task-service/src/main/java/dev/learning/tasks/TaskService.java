package dev.learning.tasks;

import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final OutboxStore outbox;
    private final ObjectMapper json;
    private final ApplicationEventPublisher events;
    public record Changed(UUID taskId) {}

    public TaskService(TaskRepository tasks, OutboxStore outbox, ObjectMapper json, ApplicationEventPublisher events) {
        this.tasks = tasks; this.outbox = outbox; this.json = json; this.events = events;
    }

    @Transactional
    public TaskView create(String title) {
        var task = tasks.saveAndFlush(new TaskEntity(title));
        append(task, "TaskCreated");
        return TaskView.from(task);
    }

    @Transactional
    public TaskView transition(UUID id, TaskEntity.Status next, long expectedVersion) {
        var task = tasks.findById(id).orElseThrow(() -> new TaskNotFound(id));
        if (task.getVersion() != expectedVersion) throw new TaskConflict("版本已变化，请重新读取任务");
        task.transition(next);
        tasks.flush(); // 让 UPDATE/乐观锁冲突发生在生成事件之前，并获取更新后的版本。
        append(task, "TaskStatusChanged");
        events.publishEvent(new Changed(id));
        return TaskView.from(task);
    }

    private void append(TaskEntity task, String type) {
        var event = new TaskEvent(1, UUID.randomUUID(), task.getId(), type, task.getVersion(), Instant.now());
        outbox.append(event, json.writeValueAsString(event));
        // 这里绝不发 Kafka：数据库提交成功、消息发送失败的间隙由 outbox 重试解决。
    }

    @Transactional(readOnly = true)
    public TaskView get(UUID id) { return TaskView.from(tasks.findById(id).orElseThrow(() -> new TaskNotFound(id))); }

    @Transactional(readOnly = true)
    public Page<TaskView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page >= 0，size 必须在 1..100");
        return tasks.findAll(PageRequest.of(page, size, Sort.by("updatedAt").descending().and(Sort.by("id")))).map(TaskView::from);
    }
}
