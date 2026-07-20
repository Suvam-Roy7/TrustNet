# Media Service

## Overview

Media Service manages file uploads and deletions for TrustNet.

Files are stored in MinIO Object Storage.

---

## Service Details

| Property          | Value        |
| ----------------- | ------------ |
| Service Name      | MediaService |
| Port              | 7075         |
| Storage           | MinIO        |
| Service Discovery | Eureka       |

---

## Supported Media Types

* Images
* Videos
* Documents

---

## APIs

### Upload File

POST /api/media/upload

Request:

multipart/form-data

Response:

{
"fileUrl": "...",
"objectName": "...",
"fileName": "...",
"size": 12345
}

---

### Delete File

DELETE /api/media?objectName={objectName}

Example:

DELETE /api/media?objectName=images/sample.jpg

---

## MinIO Configuration

### Bucket

trustnet-media

### Local Access

MinIO Console:

http://localhost:9001

### Default Credentials

Username:
minioadmin

Password:
minioadmin

---

## Upload Flow

Client
↓
Media Service
↓
MinIO
↓
File URL Returned

---

## Delete Flow

Post Service
↓
Media Service
↓
MinIO
↓
Object Deleted

---

## Features

* File Upload
* File Deletion
* Object Storage
* MinIO Integration
* Future S3 Compatibility

---

## Future Enhancements

* AWS S3 Migration
* CDN Integration
* Image Compression
* Thumbnail Generation
* Virus Scanning
