package com.social.NotificationService.Config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaRetryConfig {

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Object, Object> kafkaTemplate) {

		return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {

			String dltTopic = record.topic() + ".DLT";

			log.error("Kafka event moved to DLT. originalTopic={}, dltTopic={}, partition={}, offset={}",
					record.topic(), dltTopic, record.partition(), record.offset(), exception);

			return new TopicPartition(dltTopic, record.partition());
		});
	}

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {

		/*
		 * 2-second delay and 3 retries. Total processing attempts = 4: one original
		 * attempt plus three retries.
		 */
		FixedBackOff fixedBackOff = new FixedBackOff(2000L, 3L);

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);

		/*
		 * Invalid application data will not become valid after retrying.
		 */
		errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

		return errorHandler;
	}
}