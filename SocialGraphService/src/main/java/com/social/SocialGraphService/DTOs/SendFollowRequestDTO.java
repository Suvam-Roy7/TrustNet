package com.social.SocialGraphService.DTOs;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFollowRequestDTO {

    @NotNull(message = "Receiver user ID is required")
    private UUID receiverId;
}