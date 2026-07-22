package com.social.FeedService.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.FeedService.Repository.FeedPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostDeletedEventConsumer {

	private final FeedPostRepository feedPostRepository;

	@KafkaListener(topics = "trustnet.post.deleted.v1", groupId = "feed-service", containerFactory = "postDeletedKafkaListenerContainerFactory")
	@Transactional
	public void consume(PostDeletedEvent event) {

		log.info("Received POST_DELETED event. eventId={}, postId={}", event.eventId(), event.postId());

		boolean postExists = feedPostRepository.existsById(event.postId());

		if (!postExists) {

			log.info("Feed post was already absent. postId={}", event.postId());

			return;
		}

		feedPostRepository.deleteById(event.postId());

		log.info("Post removed from FeedService. postId={}", event.postId());
	}
}