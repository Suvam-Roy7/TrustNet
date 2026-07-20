package com.social.NotificationService.Config;

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
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.social.NotificationService.Event.FollowCreatedEvent;

@Configuration
public class FollowCreatedKafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, FollowCreatedEvent> followCreatedConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

		Map<String, Object> properties = new HashMap<>();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-follow-created");

		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		JsonDeserializer<FollowCreatedEvent> deserializer = new JsonDeserializer<>(FollowCreatedEvent.class);

		deserializer.addTrustedPackages("com.social.NotificationService.Event");

		deserializer.setUseTypeHeaders(false);

		return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
	}

	@Bean(name = "followCreatedKafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, FollowCreatedEvent> followCreatedKafkaListenerContainerFactory(

			ConsumerFactory<String, FollowCreatedEvent> followCreatedConsumerFactory,

			DefaultErrorHandler kafkaErrorHandler) {

		ConcurrentKafkaListenerContainerFactory<String, FollowCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(followCreatedConsumerFactory);

		factory.setCommonErrorHandler(kafkaErrorHandler);

		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

		return factory;
	}
}