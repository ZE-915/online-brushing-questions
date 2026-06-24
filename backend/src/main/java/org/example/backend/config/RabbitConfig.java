package org.example.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String STAT_QUEUE = "quiz.stat.update";

    @Bean
    public Queue statQueue() {
        return new Queue(STAT_QUEUE, true);
    }
}
