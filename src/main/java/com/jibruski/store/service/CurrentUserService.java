package com.jibruski.store.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.jibruski.exceptionstarter.exceptions.UnauthorizedException;
import com.jibruski.jwtauth.model.UserPrincipal;

@Component
public class CurrentUserService {

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("No authenticated user in context");
        }
        return Long.parseLong(principal.getSubject());
    }
}