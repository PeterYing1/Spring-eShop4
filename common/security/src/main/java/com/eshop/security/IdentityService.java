package com.eshop.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Spring Security implementation of {@link IIdentityService}.
 *
 * <p>Extracts the user's subject identifier from the {@code sub} claim of the
 * JWT bearer token held in {@link SecurityContextHolder}.  Mirrors the .NET
 * source's {@code IdentityService} which reads from
 * {@code HttpContext.User.FindFirst("sub")}.
 *
 * <p>Falls back to {@link org.springframework.security.core.Authentication#getName()}
 * for non-JWT authentication types (e.g. tests using
 * {@code UsernamePasswordAuthenticationToken}).
 */
@Service
public class IdentityService implements IIdentityService {

    /**
     * Returns the {@code sub} claim from the current JWT, or falls back to
     * {@code Authentication.getName()} for non-JWT principals, or {@code null}
     * when the security context holds no authentication.
     *
     * @return subject string or {@code null}
     */
    @Override
    public String getUserIdentity() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getClaimAsString("sub");
        }
        return auth.getName();
    }
}
