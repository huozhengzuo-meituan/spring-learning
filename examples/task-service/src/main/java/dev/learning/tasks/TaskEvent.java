package dev.learning.tasks;

import java.time.Instant;
import java.util.UUID;

// version 是事件 schema 版本；aggregateVersion 才是任务乐观锁版本。
public record TaskEvent(int version, UUID eventId, UUID taskId, String type,
                        long aggregateVersion, Instant occurredAt) {}
