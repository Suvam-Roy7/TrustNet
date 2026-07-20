# Auth Service

## Overview

Auth Service is responsible for user authentication and account management.

### Responsibilities

* User Registration
* User Login
* Password Encryption (BCrypt)
* JWT Token Generation
* Account Status Management

### Non-Responsibilities

The following are handled by other services:

* User Profiles (Profile Service)
* Followers / Following (Social Graph Service)
* Posts & Feed (Post Service)
* Notifications (Notification Service)
* User Verification (Verification Service)

---

# Service Information

## Application Name

```properties
spring.application.name=auth-service
```

## Port

```properties
server.port=7072
```

---

# Tech Stack

* Java 17
* Spring Boot 3.5.x
* Spring Security
* Spring Data JPA
* PostgreSQL
* JWT (JJWT)
* Lombok
* Swagger / OpenAPI
* Eureka Client

---

# API Documentation

## Swagger UI

```text
http://localhost:7072/swagger-ui/index.html
```

## OpenAPI JSON

```text
http://localhost:7072/v3/api-docs
```

---

# APIs

## Register User

### Endpoint

```http
POST /api/auth/register
```

### Request

```json
{
  "email": "user@gmail.com",
  "password": "Password@123"
}
```

### Response

```text
User Registered Successfully
```

---

## Login User

### Endpoint

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "user@gmail.com",
  "password": "Password@123"
}
```

### Response

```json
{
  "accessToken": "jwt-token"
}
```

---

# Database

## Table: users

| Column         | Type      |
| -------------- | --------- |
| id             | UUID      |
| email          | VARCHAR   |
| password       | VARCHAR   |
| email_verified | BOOLEAN   |
| account_status | VARCHAR   |
| created_at     | TIMESTAMP |
| updated_at     | TIMESTAMP |

---

# Account Status

```text
ACTIVE
SUSPENDED
DELETED
```

---

# Security

## Password Storage

Passwords are encrypted using:

```text
BCryptPasswordEncoder
```

## Authentication

Authentication is JWT-based.

Current Flow:

```text
Login
  ↓
Validate Credentials
  ↓
Generate JWT
  ↓
Return Access Token
```

JWT validation will be handled by the API Gateway.

---

# Future Enhancements

## Authentication

* Refresh Token
* Logout API
* Forgot Password
* Reset Password
* Email Verification
* Multi-Factor Authentication (MFA)

## OAuth

* Google Login
* GitHub Login
* LinkedIn Login

---

# Events

## Future Produced Events

```text
UserRegisteredEvent
UserDeletedEvent
PasswordChangedEvent
```

## Future Consumed Events

```text
EmailVerifiedEvent
```

---

# Dependencies

## Current

* PostgreSQL
* Eureka Server

## Future

* API Gateway
* Kafka
* Notification Service
* Profile Service

---

# Development Status

## Completed

* User Registration
* User Login
* BCrypt Password Encryption
* JWT Token Generation
* Swagger Documentation

## Pending

* JWT Validation
* Refresh Token
* Forgot Password
* Email Verification
* MFA
* Kafka Integration

```
```
