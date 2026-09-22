package dev.learning.tasks;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("infra")
public class TaskEventConsumer {
    private final ObjectMapper json;
    private final TaskEventProcessor processor;
    public TaskEventConsumer(ObjectMapper json, TaskEventProcessor processor) { this.json = json; this.processor = processor; }

    @KafkaListener(topics = KafkaConfiguration.TOPIC)
    public void consume(String payload) {
        // 数据库提交之后 listener 才返回，record ack 才能推进 offset。
        // 提交后、offset 前崩溃会收到重复消息；process 会识别同一个 eventId。
        processor.process(json.readValue(payload, TaskEvent.class));
    }
}
