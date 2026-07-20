# Feed Service

## Overview

Feed Service is responsible for generating the user's home timeline by aggregating posts from users they follow.

The service combines data from:

* Social Graph Service
* Post Service
* Profile Service

and returns a unified feed sorted by latest activity.

---

## Service Details

| Property          | Value       |
| ----------------- | ----------- |
| Service Name      | FeedService |
| Port              | 7077        |
| Service Discovery | Eureka      |
| Communication     | OpenFeign   |

---

## Dependencies

### Internal Services

* SocialGraphService
* PostService
* ProfileService

---

## Feed Generation Flow

User Request

↓

Get Following Users

↓

Fetch Posts From Followed Users

↓

Fetch Profile Information

↓

Sort By Created Time Descending

↓

Return Feed

---

## APIs

### Get Home Feed

GET /api/feed/{userId}

Parameters:

* page
* size

Example:

GET /api/feed/{userId}?page=0&size=20

---

## Feed Response

Contains:

* Post Id
* User Id
* Username
* Content
* Like Count
* Comment Count
* Created Time

---

## Features

* Home Timeline
* Chronological Sorting
* Aggregated User Information
* Pagination Support

---

## Future Enhancements

* Feed Ranking Algorithm
* Trust Score Ranking
* Trending Content
* Suggested Posts
* Redis Caching
* Kafka Event Driven Feed Updates
