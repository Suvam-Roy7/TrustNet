package com.social.SocialGraphService.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.social.SocialGraphService.DTOs.FollowRequestDTO;
import com.social.SocialGraphService.DTOs.FollowResponseDTO;
import com.social.SocialGraphService.DTOs.SocialStatsDTO;
import com.social.SocialGraphService.Service.SocialGraphService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialGraphController {

    private final SocialGraphService service;

    @PostMapping("/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowResponseDTO> follow(
            @Valid @RequestBody FollowRequestDTO request) {

        FollowResponseDTO response =
                service.follow(request.getFollowedUserId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollow(
            @Valid @RequestBody FollowRequestDTO request) {

        service.unfollow(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/followers/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UUID>> followers(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                service.getFollowers(userId));
    }

    @GetMapping("/following/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UUID>> following(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                service.getFollowing(userId));
    }

    @GetMapping("/stats/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SocialStatsDTO> stats(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                service.getStats(userId));
    }
}