package com.eshop.webmvc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthHelper {

    /**
     * Extracts the access token string from the current authentication.
     * For OIDC sessions, the token is stored on the OidcUser principal.
     */
    public String getAccessToken(Authentication auth) {
        if (auth == null) {
            return null;
        }
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                // Spring OAuth2 client stores the raw access token as an attribute
                Object token = oidcUser.getAttribute("access_token");
                if (token instanceof String s) {
                    return s;
                }
                // Fall back to the OIDC ID token value if access_token attribute is absent
                if (oidcUser.getIdToken() != null) {
                    return oidcUser.getIdToken().getTokenValue();
                }
            }
        }
        log.warn("Could not extract access token from authentication: {}", auth.getClass().getSimpleName());
        return null;
    }

    /**
     * Returns the preferred_username claim value, or the principal name as fallback.
     */
    public String getUsername(Authentication auth) {
        if (auth == null) {
            return "anonymous";
        }
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            Object username = principal.getAttribute("preferred_username");
            if (username instanceof String s) {
                return s;
            }
        }
        return auth.getName();
    }

    /**
     * Returns the subject (sub) claim, used as the user ID for basket operations.
     */
    public String getUserId(Authentication auth) {
        if (auth == null) {
            return null;
        }
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            Object sub = principal.getAttribute("sub");
            if (sub instanceof String s) {
                return s;
            }
        }
        return auth.getName();
    }

    /**
     * Returns a named claim attribute from the authenticated principal.
     */
    public String getClaim(Authentication auth, String claimName) {
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            Object val = oauthToken.getPrincipal().getAttribute(claimName);
            return val instanceof String s ? s : null;
        }
        return null;
    }
}
