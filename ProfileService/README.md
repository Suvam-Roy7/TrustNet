# Profile Service

## Overview

Profile Service is responsible for managing public user profile information.

This service does NOT handle:

* Authentication
* Passwords
* JWT Tokens
* User Roles

These responsibilities belong to Auth Service.

---

## Responsibilities

* Create Profile
* Get Profile
* Update Profile
* Search Profiles
* Manage Trust Level
* Manage Public User Information

---

## Tech Stack

* Java 21
* Spring Boot 3
* Spring Data JPA
* PostgreSQL
* Lombok
* Swagger/OpenAPI

---

## Swagger / OpenAPI Documentation

### Swagger UI

Local Environment:

```text
http://localhost:8081/swagger-ui.html
```

or

```text
http://localhost:8081/swagger-ui/index.html
```

### OpenAPI Specification

```text
http://localhost:8081/v3/api-docs
```

### OpenAPI YAML

```text
http://localhost:8081/v3/api-docs.yaml
```

> Replace `8081` with the configured server port if different.

---

## APIs

### Create Profile

**POST** `/api/profiles`

Request:

```json
{
  "userId": "uuid",
  "username": "suvamroy"
}
```

---

### Get Profile

**GET** `/api/profiles/{userId}`

---

### Update Profile

**PUT** `/api/profiles/{userId}`

Request:

```json
{
  "displayName": "Suvam Roy",
  "bio": "Java Backend Developer",
  "profession": "Software Engineer"
}
```

---

## Database

**Table:** `profiles`

### Fields

* profileId
* userId
* username
* displayName
* bio
* profession
* location
* website
* profilePictureUrl
* coverPictureUrl
* trustLevel
* createdAt
* updatedAt

---

## Trust Levels

* NEW_USER
* EMAIL_VERIFIED
* COMMUNITY_VERIFIED
* PROFESSIONAL_VERIFIED
* TRUSTED_MEMBER

---

## Future Enhancements

* Search API
* Kafka Integration
* Profile Verification
* Profile Analytics
* Profile View Tracking

---

## Events

### Consumes

* UserRegisteredEvent

### Produces

* ProfileUpdatedEvent
