package com.social.NotificationService.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.social.NotificationService.Entity.NotificationType;

import jakarta.persistence.Column;
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
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID recipientUserId;

    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;
    
    @Column(nullable = false, unique = true)
    private UUID eventId;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();

        if(isRead == null) {
            isRead = false;
        }
    }
}
