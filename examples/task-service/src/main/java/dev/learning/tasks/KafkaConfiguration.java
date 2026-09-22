package dev.learning.tasks;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@Profile("infra")
@EnableScheduling
public class KafkaConfiguration {
    public static final String TOPIC = "learning.task-events.v1";

    @Bean NewTopic taskEventsTopic() {
        // 单副本只为本地实验，不能承受 broker 丢失；生产要结合集群规划副本和 min.insync.replicas。
        return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build();
    }

    @Bean DefaultErrorHandler kafkaErrorHandler() {
        // 明确保留失败消息：无限重试会阻塞该分区，必须配套告警。
        // 下一步练习是加 DLT、有限重试和人工重放；不要用空 recoverer 静默跳过失败事件。
        var handler = new DefaultErrorHandler((record, error) -> {
            // 即便某异常被框架归为 fatal，也不能默认 log 后跳过；先保留，待人工修复/重放。
            throw new IllegalStateException("事件尚未恢复，禁止提交其 offset", error);
        }, new FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        return handler;
    }
}
