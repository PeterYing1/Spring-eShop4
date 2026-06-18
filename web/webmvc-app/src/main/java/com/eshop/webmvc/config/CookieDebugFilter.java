package com.eshop.webmvc.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CookieDebugFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CookieDebugFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();
        if (!uri.startsWith("/css") && !uri.startsWith("/js") && !uri.startsWith("/img") && !uri.equals("/favicon.ico")) {
            Cookie[] cookies = request.getCookies();
            String jsessionid = "NOT_PRESENT";
            String xsrfToken = "NOT_PRESENT";
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("JSESSIONID".equals(c.getName())) {
                        String v = c.getValue();
                        jsessionid = v.substring(0, Math.min(8, v.length())) + "...";
                    } else if ("XSRF-TOKEN".equals(c.getName())) {
                        String v = c.getValue();
                        xsrfToken = v.substring(0, Math.min(8, v.length())) + "...";
                    }
                }
            }
            log.debug("COOKIES [{} {}] JSESSIONID={} XSRF-TOKEN={}",
                    request.getMethod(), uri, jsessionid, xsrfToken);
        }
        chain.doFilter(req, res);
    }
}
