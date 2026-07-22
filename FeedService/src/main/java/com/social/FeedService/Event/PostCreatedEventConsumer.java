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
public class PostCreatedEventConsumer {

	private final FeedPostRepository feedPostRepository;

	@KafkaListener(topics = "trustnet.post.created.v1", groupId = "feed-service")
	@Transactional
	public void consume(PostCreatedEvent event) {

		log.info("Received POST_CREATED event. eventId={}, postId={}", event.eventId(), event.postId());

		boolean eventAlreadyProcessed = feedPostRepository.existsByEventId(event.eventId());

		boolean postAlreadyExists = feedPostRepository.existsById(event.postId());

		if (eventAlreadyProcessed || postAlreadyExists) {

			log.info("Duplicate POST_CREATED event ignored. " + "eventId={}, postId={}", event.eventId(),
					event.postId());

			return;
		}

		FeedPost feedPost = FeedPost.builder().eventId(event.eventId()).postId(event.postId())
				.authorId(event.authorId())

				/*
				 * Keep null until authorUsername is included in PostCreatedEvent.
				 */
				.authorUsername(null)

				.content(event.content()).likeCount(0L).commentCount(0L).createdAt(event.createdAt())
				.updatedAt(event.createdAt()).build();

		feedPostRepository.save(feedPost);

		log.info("Post saved in FeedService. postId={}, authorId={}", event.postId(), event.authorId());
	}
}