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

import com.social.FeedService.Event.PostDeletedEvent;

@Configuration
public class PostDeletedKafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, PostDeletedEvent> postDeletedConsumerFactory(

			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

		Map<String, Object> properties = new HashMap<>();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "feed-service");

		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		JsonDeserializer<PostDeletedEvent> valueDeserializer = new JsonDeserializer<>(PostDeletedEvent.class);

		valueDeserializer.addTrustedPackages("com.social.FeedService.Event");

		valueDeserializer.setUseTypeHeaders(false);

		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
	}

	@Bean(name = "postDeletedKafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, PostDeletedEvent> postDeletedKafkaListenerContainerFactory(

			ConsumerFactory<String, PostDeletedEvent> postDeletedConsumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, PostDeletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(postDeletedConsumerFactory);

		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

		return factory;
	}
}