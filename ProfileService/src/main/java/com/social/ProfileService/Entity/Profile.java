package com.social.ProfileService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue
    private UUID profileId;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String displayName;

    @Column(length = 500)
    private String bio;

    private String profession;

    private String location;

    private String website;

    private String profilePictureUrl;

    private String coverPictureUrl;

    @Enumerated(EnumType.STRING)
    private TrustLevel trustLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if(trustLevel == null) {
            trustLevel = TrustLevel.NEW_USER;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}