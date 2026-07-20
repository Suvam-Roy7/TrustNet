package com.social.PostService.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	public static final String POST_CREATED_TOPIC = "trustnet.post.created.v1";

	@Bean
	public NewTopic postCreatedTopic() {

		return TopicBuilder.name(POST_CREATED_TOPIC).partitions(3).replicas(1).build();
	}

	public static final String POST_DELETED_TOPIC = "trustnet.post.deleted.v1";

	@Bean
	public NewTopic postDeletedTopic() {

		return TopicBuilder.name(POST_DELETED_TOPIC).partitions(3).replicas(1).build();
	}

	public static final String POST_UPDATED_TOPIC = "trustnet.post.updated.v1";

	@Bean
	public NewTopic postUpdatedTopic() {

		return TopicBuilder.name(POST_UPDATED_TOPIC).partitions(3).replicas(1).build();
	}

	public static final String POST_LIKED_TOPIC = "trustnet.post.liked.v1";

	@Bean
	public NewTopic postLikedTopic() {

		return TopicBuilder.name(POST_LIKED_TOPIC).partitions(3).replicas(1).build();
	}

	public static final String COMMENT_CREATED_TOPIC = "trustnet.comment.created.v1";

	@Bean
	public NewTopic commentCreatedTopic() {

		return TopicBuilder.name(COMMENT_CREATED_TOPIC).partitions(3).replicas(1).build();
	}

	public static final String MENTION_CREATED_TOPIC = "trustnet.mention.created.v1";

	@Bean
	public NewTopic mentionCreatedTopic() {

		return TopicBuilder.name(MENTION_CREATED_TOPIC).partitions(3).replicas(1).build();
	}
}