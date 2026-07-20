package com.social.ProfileService.Components;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("profileSecurity")
public class ProfileSecurity {

    public boolean isOwner(UUID userId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return userId.toString()
                .equals(authentication.getName());
    }
}
