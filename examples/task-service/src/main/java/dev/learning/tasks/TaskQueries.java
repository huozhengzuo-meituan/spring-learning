package dev.learning.tasks;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskQueries {
    private final TaskService tasks;
    private final TaskCache cache;
    private final JdbcTemplate jdbc;

    public TaskQueries(TaskService tasks, TaskCache cache, JdbcTemplate jdbc) {
        this.tasks = tasks; this.cache = cache; this.jdbc = jdbc;
    }

    public TaskView get(UUID id) {
        // 查询编排属于应用层。Controller 只处理 HTTP，TaskService 不反向依赖查询服务。
        // 缓存网络访问在数据库读事务之外，避免拿着数据库连接等待 Redis。
        return cache.find(id).orElseGet(() -> {
            var task = tasks.get(id);
            cache.put(task);
            return task;
        });
    }

    public Page<TaskView> list(int page, int size) { return tasks.list(page, size); }

    public long processedEventCount(UUID id) {
        tasks.get(id); // 资源不存在时保持与单条查询一致的 404 语义。
        var counts = jdbc.query("SELECT event_count FROM task_event_counts WHERE task_id=?", (rs, row) -> rs.getLong(1), id);
        return counts.isEmpty() ? 0L : counts.getFirst();
    }
}
