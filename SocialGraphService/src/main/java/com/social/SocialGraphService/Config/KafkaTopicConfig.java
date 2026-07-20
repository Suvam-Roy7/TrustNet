package com.social.SocialGraphService.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String FOLLOW_CREATED_TOPIC =
            "trustnet.follow.created.v1";

    @Bean
    public NewTopic followCreatedTopic() {

        return TopicBuilder
                .name(FOLLOW_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}