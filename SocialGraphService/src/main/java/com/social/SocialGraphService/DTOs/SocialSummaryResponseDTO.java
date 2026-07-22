package com.social.SocialGraphService.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSummaryResponseDTO {

    private long followingCount;

    private long followerCount;

    private long pendingIncomingRequestCount;
}