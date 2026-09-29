package uk.gov.hmcts.cp.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ExemptPathPolicy {

    private static final Set<String> EXEMPT_PATHS = Set.of(
        "/",
        "/health",
        "/health/liveness",
        "/health/readiness",
        "/info",
        "/prometheus"
    );

    public Set<String> exemptPaths() {
        return EXEMPT_PATHS;
    }

    public boolean isExempt(final String path) {
        return EXEMPT_PATHS.contains(stripTrailingSlash(path));
    }

    /** Spring serves health at {@code /health/} as well, so a trailing slash must not defeat the match. */
    private static String stripTrailingSlash(final String path) {
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
