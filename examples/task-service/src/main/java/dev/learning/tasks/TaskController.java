package dev.learning.tasks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService tasks;
    private final TaskQueries queries;
    public TaskController(TaskService tasks, TaskQueries queries) {
        this.tasks = tasks; this.queries = queries;
    }
    public record CreateTask(@NotBlank @Size(max = 120) String title) {}
    public record ChangeStatus(@NotNull TaskEntity.Status status, @NotNull @PositiveOrZero Long version) {}
    public record TaskPage(List<TaskView> content, int page, int size, long totalElements, int totalPages) {}

    @PostMapping
    public ResponseEntity<TaskView> create(@Valid @RequestBody CreateTask request) {
        var task = tasks.create(request.title());
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @GetMapping("/{id}")
    public TaskView get(@PathVariable UUID id) {
        return queries.get(id);
    }

    @GetMapping
    public TaskPage list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = queries.list(page, size);
        // 不直接序列化 PageImpl，把分页协议作为自己的 API 契约。
        return new TaskPage(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
    }

    @PatchMapping("/{id}/status")
    public TaskView transition(@PathVariable UUID id, @Valid @RequestBody ChangeStatus request) {
        return tasks.transition(id, request.status(), request.version());
    }

    @GetMapping("/{id}/events-count")
    public Map<String, Long> eventCount(@PathVariable UUID id) {
        return Map.of("processedEvents", queries.processedEventCount(id));
    }
}
