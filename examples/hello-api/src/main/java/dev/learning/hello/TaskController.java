package dev.learning.hello;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskStore store;

    // 构造注入：依赖显式可见，也能在测试中直接 new；不需要字段上的 @Autowired。
    public TaskController(TaskStore store) { this.store = store; }

    public record CreateTask(@NotBlank @Size(max = 120) String title) {}

    @PostMapping
    public ResponseEntity<TaskStore.Task> create(@Valid @RequestBody CreateTask request) {
        var task = store.create(request.title());
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @GetMapping public List<TaskStore.Task> list() { return store.list(); }
    @GetMapping("/{id}") public TaskStore.Task get(@PathVariable UUID id) { return store.get(id); }
}
