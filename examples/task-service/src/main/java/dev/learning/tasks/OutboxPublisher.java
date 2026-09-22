package dev.learning.tasks;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("infra")
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxStore outbox;
    private final KafkaTemplate<String, String> kafka;
    public OutboxPublisher(OutboxStore outbox, KafkaTemplate<String, String> kafka) { this.outbox = outbox; this.kafka = kafka; }

    @Scheduled(fixedDelayString = "${learning.outbox.interval-ms:2000}")
    public void publishPending() {
        for (var event : outbox.pending()) {
            try {
                // 网络等待不占用数据库写事务；Kafka ACK 后才标记 published。
                // ACK 后进程崩溃会重发：这正是消费者需要数据库幂等的原因。
                kafka.send(KafkaConfiguration.TOPIC, event.taskId().toString(), event.payload()).get(12, TimeUnit.SECONDS);
                outbox.markPublished(event.eventId());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                log.warn("Outbox publish failed; event remains pending: {}", event.eventId(), ex);
                return;
            }
        }
    }
}
