package dev.learning.tasks;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!infra")
public class NoTaskCache implements TaskCache {
    public Optional<TaskView> find(UUID id) { return Optional.empty(); }
    public void put(TaskView task) {}
}
