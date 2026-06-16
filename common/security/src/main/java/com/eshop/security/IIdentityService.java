package com.eshop.security;

/**
 * Abstraction over the current user's identity.
 *
 * <p>For API services (resource servers) the identity is extracted from the
 * {@code sub} claim of the incoming JWT bearer token.
 */
public interface IIdentityService {

    /**
     * Returns the subject identifier ({@code sub} claim) of the currently
     * authenticated user.
     *
     * @return the subject string (a UUID in Keycloak), or {@code null} if no
     *         authentication is present in the security context
     */
    String getUserIdentity();
}
