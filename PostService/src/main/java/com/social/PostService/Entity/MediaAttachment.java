package com.social.PostService.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAttachment {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID postId;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private LocalDateTime createdAt;
    
    private String objectName;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
