package com.ducknife.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ducknife.project.common.exception.UnauthorizedException;

public class SecurityUtils {

    public static CustomUserDetails getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null 
            || !authentication.isAuthenticated() 
            || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userDetails;
    }
}
