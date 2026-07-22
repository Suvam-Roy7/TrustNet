package com.social.SocialGraphService.DTOs;

import com.social.SocialGraphService.Entity.FollowRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatusResponseDTO {

    private boolean following;

    private boolean requestPending;

    private FollowRequestStatus requestStatus;
}