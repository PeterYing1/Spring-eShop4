package com.eshop.webspa.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards Angular client-side routes to {@code index.html}.
 *
 * <p>Spring's static-resource handler serves real files (JS, CSS, images, etc.) before this
 * controller is consulted. This controller only fires for paths that do not map to an existing
 * static file.
 *
 * <p>The regex {@code [^\\.]*} matches path segments that contain <em>no dot</em>, so file
 * extension requests (e.g. {@code /main.abc123.js}) are never forwarded here — they are
 * either served by the resource handler or result in a 404.
 */
@Controller
public class SpaController {

    /**
     * Root path — serve Angular's entry point directly.
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Single-segment Angular routes (e.g. {@code /catalog}, {@code /basket}, {@code /orders}).
     *
     * <p>The pattern {@code /{path:[^\\.]*}} matches any single path segment that contains no
     * dot, ensuring that real asset requests (which always contain a file extension) are never
     * captured here.
     */
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String spaRouteSingle() {
        return "forward:/index.html";
    }

    /**
     * Multi-segment Angular routes (e.g. {@code /orders/123}, {@code /catalog/details/5}).
     *
     * <p>The wildcard {@code /**} after the first extension-free segment captures any depth of
     * nested Angular routes while still excluding static asset paths.
     */
    @RequestMapping(value = "/{path:[^\\.]*}/**")
    public String spaRouteNested() {
        return "forward:/index.html";
    }
}
