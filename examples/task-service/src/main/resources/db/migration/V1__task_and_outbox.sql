-- 同一份 migration 在 H2 PostgreSQL mode 和真实 PostgreSQL 上执行。
-- H2 只让入门反馈更快；不能替代 PostgreSQL 的并发、SQL 语义验收。
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    status VARCHAR(24) NOT NULL,
    version BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT task_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'))
);
CREATE INDEX idx_tasks_updated ON tasks(updated_at, id);

CREATE TABLE outbox_events (
    event_id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id),
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_outbox_pending ON outbox_events(published_at, created_at);

-- event_id 是去重边界，而不是 task_id：同一任务允许多次不同的业务事件。
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE task_event_counts (
    task_id UUID PRIMARY KEY,
    event_count BIGINT NOT NULL
);
