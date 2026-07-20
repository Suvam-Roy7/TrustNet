# Social Graph Service

## Overview

Social Graph Service manages relationships between users.

### Responsibilities

* Follow User
* Unfollow User
* Get Followers
* Get Following
* Get Social Statistics

### Non Responsibilities

* Authentication
* User Profiles
* Posts
* Notifications

---

# Service Information

Application Name:

social-graph-service

Port:

7073

---

# APIs

## Follow User

POST /api/social/follow

Request

{
"followerId":"uuid1",
"followingId":"uuid2"
}

---

## Unfollow User

DELETE /api/social/unfollow

Request

{
"followerId":"uuid1",
"followingId":"uuid2"
}

---

## Get Followers

GET /api/social/followers/{userId}

---

## Get Following

GET /api/social/following/{userId}

---

## Get Stats

GET /api/social/stats/{userId}

Response

{
"followers":10,
"following":20
}

---

# Database

Table: follows

Fields:

* id
* followerId
* followingId
* createdAt

Unique Constraint:

(followerId, followingId)

---

# Future Enhancements

* Block User
* Mute User
* Close Friend
* Mentor Connection
* Alumni Connection
* Connection Recommendations

---

# Dependencies

Current

* PostgreSQL
* Eureka Server

Future

* Auth Service
* Notification Service
* Kafka

---

# Development Status

Completed

* Follow User
* Unfollow User
* Followers API
* Following API
* Social Stats API
* Validation
* Global Exception Handling

Pending

* Auth Service Validation
* Feign Integration
* Kafka Events
* Recommendation Engine
