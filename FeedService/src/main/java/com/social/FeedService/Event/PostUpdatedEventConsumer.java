package com.social.FeedService.Event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.social.FeedService.Entity.FeedPost;
import com.social.FeedService.Repository.FeedPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostUpdatedEventConsumer {

	private final FeedPostRepository feedPostRepository;

	@KafkaListener(topics = "trustnet.post.updated.v1", groupId = "feed-service-post-updated", containerFactory = "postUpdatedKafkaListenerContainerFactory")
	@Transactional
	public void consume(PostUpdatedEvent event) {

		log.info("Received POST_UPDATED event. eventId={}, postId={}", event.eventId(), event.postId());

		FeedPost feedPost = feedPostRepository.findByPostId(event.postId()).orElse(null);

		if (feedPost == null) {

			log.warn("Feed post not found for update. postId={}", event.postId());

			return;
		}

		/*
		 * Ignore duplicate or older update events.
		 */
		if (feedPost.getUpdatedAt() != null && !event.updatedAt().isAfter(feedPost.getUpdatedAt())) {

			log.info("Duplicate or outdated POST_UPDATED event ignored. postId={}", event.postId());

			return;
		}

		feedPost.setContent(event.content());
		feedPost.setUpdatedAt(event.updatedAt());

		feedPostRepository.save(feedPost);

		log.info("Feed post updated successfully. postId={}", event.postId());
	}
}