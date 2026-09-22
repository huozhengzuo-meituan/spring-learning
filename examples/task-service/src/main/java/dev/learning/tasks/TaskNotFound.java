package dev.learning.tasks;

import java.util.UUID;

public class TaskNotFound extends RuntimeException {
    public TaskNotFound(UUID id) { super("任务不存在：" + id); }
}
