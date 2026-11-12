package com.annadhata.util;

import com.annadhata.exception.UnauthorizedException;
import com.annadhata.model.User;
import com.annadhata.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return principal.getUser();
    }

    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
