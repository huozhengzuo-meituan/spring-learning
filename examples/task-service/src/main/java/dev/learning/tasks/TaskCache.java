package dev.learning.tasks;

import java.util.Optional;
import java.util.UUID;

public interface TaskCache {
    Optional<TaskView> find(UUID id);
    void put(TaskView task);
}
