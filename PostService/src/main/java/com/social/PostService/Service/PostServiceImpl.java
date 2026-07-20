package com.social.PostService.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.social.PostService.Client.AuthClient;
import com.social.PostService.Client.MediaClient;
import com.social.PostService.Client.ProfileClient;
import com.social.PostService.DTOs.*;
import com.social.PostService.Entity.Hashtag;
import com.social.PostService.Entity.MediaAttachment;
import com.social.PostService.Entity.Mention;

import com.social.PostService.Entity.Post;
import com.social.PostService.Entity.PostHashtag;
import com.social.PostService.Entity.PostLike;
import com.social.PostService.Entity.PostMention;
import com.social.PostService.Event.PostCreatedDomainEvent;
import com.social.PostService.Event.PostDeletedDomainEvent;
import com.social.PostService.Event.PostUpdatedDomainEvent;
import com.social.PostService.Exception.PostNotFoundException;
import com.social.PostService.Exception.UnauthorizedActionException;
import com.social.PostService.Repository.CommentRepository;
import com.social.PostService.Repository.HashtagRepository;
import com.social.PostService.Repository.MediaAttachmentRepository;
import com.social.PostService.Repository.MentionRepository;
import com.social.PostService.Repository.PostHashtagRepository;
import com.social.PostService.Repository.PostLikeRepository;
import com.social.PostService.Repository.PostMentionRepository;
import com.social.PostService.Repository.PostRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.social.PostService.Event.MentionCreatedDomainEvent;

import com.social.PostService.Event.PostLikedDomainEvent;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

	private final PostRepository repository;

	private final PostLikeRepository postLikeRepository;

	private final AuthClient authClient;

	private final CommentRepository commentRepository;

	private final HashtagRepository hashtagRepository;

	private final PostHashtagRepository postHashtagRepository;

	private final MentionRepository mentionRepository;

	private final PostMentionRepository postMentionRepository;

	private final MediaAttachmentRepository mediaAttachmentRepository;

	private final ProfileClient profileClient;

	private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

	private final ApplicationEventPublisher eventPublisher;
	
	private final MediaClient mediaClient;

	@Override
	@Transactional
	public PostResponseDTO createPost(UUID userId, CreatePostRequestDTO request) {

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {
			throw new RuntimeException("User account is inactive");
		}

		Post post = Post.builder().userId(userId).content(request.getContent()).build();

		Post savedPost = repository.save(post);

		/*
		 * Save media attachments.
		 */
		if (request.getAttachments() != null) {

			for (MediaAttachmentDTO dto : request.getAttachments()) {

				MediaAttachment attachment = MediaAttachment.builder().postId(savedPost.getId())
						.fileUrl(dto.getFileUrl()).objectName(dto.getObjectName()).mediaType(dto.getMediaType())
						.build();

				mediaAttachmentRepository.save(attachment);
			}
		}

		/*
		 * Save hashtags.
		 */
		Set<String> hashtags = extractHashtags(savedPost.getContent());

		for (String tag : hashtags) {

			Hashtag hashtag = hashtagRepository.findByTag(tag)
					.orElseGet(() -> hashtagRepository.save(Hashtag.builder().tag(tag).build()));

			PostHashtag postHashtag = PostHashtag.builder().postId(savedPost.getId()).hashtagId(hashtag.getId())
					.build();

			postHashtagRepository.save(postHashtag);
		}

		/*
		 * Save mentions and publish mention events.
		 */
		Set<String> mentions = extractMentions(savedPost.getContent());

		if (!mentions.isEmpty()) {

			ProfileResponseDTO authorProfile = profileClient.getProfile(savedPost.getUserId());

			for (String username : mentions) {

				try {

					ProfileResponseDTO mentionedUser = profileClient.getByUsername(username);

					Mention mention = mentionRepository.findByUsername(username)
							.orElseGet(() -> mentionRepository.save(Mention.builder().username(username).build()));

					PostMention postMention = PostMention.builder().postId(savedPost.getId()).mentionId(mention.getId())
							.build();

					postMentionRepository.save(postMention);

					boolean selfMention = mentionedUser.getUserId().equals(savedPost.getUserId());

					if (!selfMention) {

						eventPublisher.publishEvent(new MentionCreatedDomainEvent(UUID.randomUUID(), savedPost.getId(),
								savedPost.getUserId(), mentionedUser.getUserId(), authorProfile.getUsername(),
								Instant.now()));
					}

				} catch (FeignException.NotFound ex) {

					logger.warn("Mentioned username '{}' not found", username);
				}
			}
		}

		Instant createdAt = savedPost.getCreatedAt().toInstant(ZoneOffset.UTC);

		/*
		 * Publish the post-created event.
		 */
		eventPublisher.publishEvent(new PostCreatedDomainEvent(UUID.randomUUID(), savedPost.getId(),
				savedPost.getUserId(), savedPost.getContent(), createdAt));

		return mapToDTO(savedPost);
	}

	@Cacheable(value = "posts", key = "#postId")
	@Override
	public PostResponseDTO getPost(UUID postId) {

		Post post = repository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));

		return mapToDTO(post);
	}

	@Override
	public Page<PostResponseDTO> getUserPosts(UUID userId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::mapToDTO);
	}

	@Override
	@Transactional
	public void deletePost(UUID postId) {

		UUID loggedInUserId = getLoggedInUserId();

		Post post = repository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));

		if (!post.getUserId().equals(loggedInUserId)) {
			throw new UnauthorizedActionException("You are not allowed to delete this post");
		}

		List<MediaAttachment> attachments = mediaAttachmentRepository.findByPostId(postId);

		for (MediaAttachment attachment : attachments) {

			try {
				mediaClient.deleteFile(attachment.getObjectName());

			} catch (Exception ex) {
				logger.error("Failed to delete media object: {}", attachment.getObjectName(), ex);
			}
		}

		mediaAttachmentRepository.deleteByPostId(postId);

		// Existing comments, likes, hashtags and mentions cleanup.

		repository.delete(post);

		eventPublisher
				.publishEvent(new PostDeletedDomainEvent(UUID.randomUUID(), postId, post.getUserId(), Instant.now()));
	}

	private PostResponseDTO mapToDTO(Post post) {

		return PostResponseDTO.builder()

				.id(post.getId()).userId(post.getUserId()).content(post.getContent())

				.likeCount(postLikeRepository.countByPostId(post.getId()))

				.commentCount(commentRepository.countByPostId(post.getId()))

				.hashtags(getHashtags(post.getId()))

				.mentions(getMentions(post.getId()))

				.attachments(getAttachments(post.getId()))

				.createdAt(post.getCreatedAt())

				.build();
	}

	@Override
	@CachePut(value = "posts", key = "#postId")
	public PostResponseDTO updatePost(UUID postId, UpdatePostRequestDTO request) {

		UUID loggedInUserId = getLoggedInUserId();

		Post post = repository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));

		if (!post.getUserId().equals(loggedInUserId)) {
			throw new UnauthorizedActionException("You are not allowed to update this post");
		}

		post.setContent(request.getContent());

		Post updatedPost = repository.save(post);

		Instant eventUpdatedAt = updatedPost.getUpdatedAt().toInstant(ZoneOffset.UTC);

		eventPublisher.publishEvent(new PostUpdatedDomainEvent(UUID.randomUUID(), updatedPost.getId(),
				updatedPost.getUserId(), updatedPost.getContent(), eventUpdatedAt));

		return mapToDTO(updatedPost);
	}

	@Override
	@Transactional
	public void likePost(UUID postId, UUID userId) {

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {
			throw new RuntimeException("User account is inactive");
		}

		Post post = repository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));

		boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

		if (alreadyLiked) {
			throw new RuntimeException("Post is already liked");
		}

		PostLike postLike = PostLike.builder().postId(postId).userId(userId).build();

		postLikeRepository.save(postLike);

		// Do not notify users when they like their own post.
		if (!post.getUserId().equals(userId)) {

			ProfileResponseDTO actorProfile = profileClient.getProfile(userId);

			PostLikedDomainEvent event = new PostLikedDomainEvent(UUID.randomUUID(), post.getId(), userId,
					post.getUserId(), actorProfile.getUsername(), Instant.now());

			eventPublisher.publishEvent(event);
		}
	}

	@Override
	public void unlikePost(UUID postId, UUID userId) {

		UserResponseDTO user = authClient.getUser(userId);

		if (!"ACTIVE".equals(user.getAccountStatus())) {

			throw new RuntimeException("User account is inactive");
		}

		PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
				.orElseThrow(() -> new RuntimeException("Like not found"));

		postLikeRepository.delete(like);
	}

	@Override
	public long getLikeCount(UUID postId) {

		return postLikeRepository.countByPostId(postId);
	}

	private Set<String> extractHashtags(String content) {

		Set<String> hashtags = new HashSet<>();

		Pattern pattern = Pattern.compile("#(\\w+)");

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {

			hashtags.add(matcher.group(1));
		}

		return hashtags;
	}

	private List<String> getHashtags(UUID postId) {

		List<PostHashtag> mappings = postHashtagRepository.findByPostId(postId);

		return mappings.stream()
				.map(mapping -> hashtagRepository.findById(mapping.getHashtagId()).map(Hashtag::getTag).orElse(null))
				.filter(Objects::nonNull).toList();
	}

	private Set<String> extractMentions(String content) {

		Set<String> mentions = new HashSet<>();

		Pattern pattern = Pattern.compile("@(\\w+)");

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {

			mentions.add(matcher.group(1));
		}

		return mentions;
	}

	private List<String> getMentions(UUID postId) {

		List<PostMention> mappings = postMentionRepository.findByPostId(postId);

		return mappings.stream().map(
				mapping -> mentionRepository.findById(mapping.getMentionId()).map(Mention::getUsername).orElse(null))
				.filter(Objects::nonNull).toList();
	}

	private List<MediaResponseDTO> getAttachments(UUID postId) {

		return mediaAttachmentRepository.findByPostId(postId).stream().map(media ->

		MediaResponseDTO.builder().fileUrl(media.getFileUrl()).mediaType(media.getMediaType()).build()).toList();
	}

	@Override
	public List<PostResponseDTO> getAllUserPosts(UUID userId) {

		return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::mapToDTO).toList();
	}

	private UUID getLoggedInUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {

			throw new UnauthorizedActionException("User is not authenticated");
		}

		try {
			return UUID.fromString(authentication.getName());

		} catch (IllegalArgumentException ex) {

			throw new UnauthorizedActionException("Invalid authenticated user ID");
		}
	}
}
