package dev.learning.hello;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TaskStore {
    // Web 请求会并发执行；普通 HashMap 不是线程安全的。重启后数据消失是本课有意的限制。
    private final ConcurrentHashMap<UUID, Task> tasks = new ConcurrentHashMap<>();

    public record Task(UUID id, String title) {}

    public Task create(String title) {
        var task = new Task(UUID.randomUUID(), title.strip());
        tasks.put(task.id(), task);
        return task;
    }

    public Task get(UUID id) {
        var task = tasks.get(id);
        if (task == null) throw new TaskNotFound(id);
        return task;
    }

    public List<Task> list() {
        return tasks.values().stream().sorted(Comparator.comparing(Task::id)).toList();
    }

    static class TaskNotFound extends RuntimeException {
        TaskNotFound(UUID id) { super("任务不存在：" + id); }
    }
}
