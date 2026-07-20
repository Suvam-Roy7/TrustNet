package com.social.SocialGraphService.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocialStatsDTO {

    private long followers;

    private long following;
}