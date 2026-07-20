# Post Service

## Overview

Post Service is responsible for managing user posts and social interactions around posts.

Features include:

* Create Post
* Get Post
* Update Post
* Delete Post
* Like / Unlike Post
* Comment Management
* Hashtag Extraction
* Mention Extraction
* Media Attachments
* Notification Integration

---

## Service Details

| Property          | Value        |
| ----------------- | ------------ |
| Service Name      | PostService  |
| Port              | 7074         |
| Database          | PostgreSQL   |
| Service Discovery | Eureka       |
| Communication     | Feign Client |

---

## Dependencies

### Internal Services

* Auth Service
* Profile Service
* Media Service
* Notification Service

---

## APIs

### Create Post

POST /api/posts

### Get Post

GET /api/posts/{postId}

### Update Post

PUT /api/posts/{postId}

### Delete Post

DELETE /api/posts/{postId}

### Get User Posts

GET /api/posts/user/{userId}?page=0&size=10

### Like Post

POST /api/posts/{postId}/like/{userId}

### Unlike Post

DELETE /api/posts/{postId}/like/{userId}

### Get Like Count

GET /api/posts/{postId}/likes/count

---

## Features

### Hashtag Extraction

Example:

#springboot #java

Automatically stored and linked to posts.

### Mention Extraction

Example:

@john

Creates mention mappings and notification events.

### Media Attachments

Supports:

* Image
* Video
* Document

Files are stored in MinIO through Media Service.

---

## Notification Integration

Automatically creates notifications for:

* Post Like
* Post Comment
* User Mention

---

## Database Tables

* posts
* post_likes
* comments
* hashtags
* post_hashtags
* mentions
* post_mentions
* media_attachments

---

## Future Enhancements

* Post Search
* Trending Hashtags
* Feed Ranking
* Kafka Event Publishing
* Elasticsearch Integration
