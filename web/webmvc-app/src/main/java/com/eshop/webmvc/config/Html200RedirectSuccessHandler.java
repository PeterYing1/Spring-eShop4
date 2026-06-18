package com.eshop.webmvc.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Returns a 200 HTML page with a JavaScript redirect after OAuth2 login, instead of
 * the normal 302. This is necessary because Chrome treats cookies first set in a 302
 * redirect response as "temporary" — only sent for the immediate redirect target —
 * and reverts to previously established cookies for subsequent same-page requests.
 *
 * With a 200 response, the JSESSIONID created during this request's auth processing
 * is persisted as a normal first-party cookie, and Chrome includes it in all
 * subsequent same-site requests (including form POSTs).
 */
public class Html200RedirectSuccessHandler implements AuthenticationSuccessHandler {

    private final String targetUrl;

    public Html200RedirectSuccessHandler(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
            "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
            "<script>window.location.replace('" + targetUrl + "');</script>" +
            "</body></html>"
        );
    }
}
