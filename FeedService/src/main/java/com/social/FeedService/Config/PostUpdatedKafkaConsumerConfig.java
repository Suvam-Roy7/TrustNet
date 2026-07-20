package com.social.FeedService.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.social.FeedService.Event.PostUpdatedEvent;

@Configuration
public class PostUpdatedKafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, PostUpdatedEvent> postUpdatedConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

		Map<String, Object> properties = new HashMap<>();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "feed-service-post-updated");

		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		JsonDeserializer<PostUpdatedEvent> deserializer = new JsonDeserializer<>(PostUpdatedEvent.class);

		deserializer.addTrustedPackages("com.social.FeedService.Event");

		deserializer.setUseTypeHeaders(false);

		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
	}

	@Bean(name = "postUpdatedKafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, PostUpdatedEvent> postUpdatedKafkaListenerContainerFactory(
			ConsumerFactory<String, PostUpdatedEvent> postUpdatedConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, PostUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(postUpdatedConsumerFactory);

		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

		return factory;
	}
}