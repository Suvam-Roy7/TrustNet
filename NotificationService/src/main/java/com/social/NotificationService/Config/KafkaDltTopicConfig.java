package com.social.NotificationService.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaDltTopicConfig {

	@Bean
	public NewTopic followCreatedDltTopic() {

		return createDltTopic("trustnet.follow.created.v1.DLT");
	}

	@Bean
	public NewTopic postLikedDltTopic() {

		return createDltTopic("trustnet.post.liked.v1.DLT");
	}

	@Bean
	public NewTopic commentCreatedDltTopic() {

		return createDltTopic("trustnet.comment.created.v1.DLT");
	}

	@Bean
	public NewTopic mentionCreatedDltTopic() {

		return createDltTopic("trustnet.mention.created.v1.DLT");
	}

	private NewTopic createDltTopic(String topicName) {

		return TopicBuilder.name(topicName).partitions(3).replicas(1).build();
	}
}