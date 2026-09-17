package uk.gov.hmcts.cp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ExemptPathPolicyTest {

    private static final String SPEC_RESOURCES = "classpath*:openapi/openapi-spec.yml";
    private static final String SPEC_TITLE = "Common Platform API Refdata Court Hearing Court Houses";

    private final ExemptPathPolicy policy = new ExemptPathPolicy();

    @Test
    @DisplayName("Every exempt path is exempt")
    void exempts_only_the_enumerated_infrastructure_paths() {
        assertThat(policy.exemptPaths())
            .containsExactlyInAnyOrder("/", "/health", "/health/liveness", "/health/readiness", "/info", "/prometheus");
        policy.exemptPaths().forEach(path -> assertThat(policy.isExempt(path)).isTrue());
    }

    @Test
    @DisplayName("Near-miss paths are not exempt")
    void does_not_exempt_near_miss_paths() {
        assertThat(policy.isExempt("/healthx")).isFalse();
        assertThat(policy.isExempt("/health/x")).isFalse();
        assertThat(policy.isExempt("/health/")).isFalse();
        assertThat(policy.isExempt("/HEALTH")).isFalse();
        assertThat(policy.isExempt("/prometheus/metrics")).isFalse();
        assertThat(policy.isExempt("/courthouses")).isFalse();
    }

    @Test
    @DisplayName("Every path in the contract is protected, so a new endpoint fails until it is classified")
    void protects_every_path_declared_in_the_contract() throws IOException {
        final List<String> contractPaths = contractPaths();

        assertThat(contractPaths).isNotEmpty();
        contractPaths.forEach(path -> assertThat(policy.isExempt(concretePath(path)))
            .withFailMessage("contract path %s is exempt from token validation but has not been justified", path)
            .isFalse());
    }

    private static String concretePath(final String templatedPath) {
        return templatedPath.replaceAll("\\{[^}]+}", "00000000-0000-0000-0000-000000000001");
    }

    @SuppressWarnings("unchecked")
    private static List<String> contractPaths() throws IOException {
        // Every api-cp artefact packages its spec at the same resource path, so select by info.title
        // rather than taking whichever the classloader reaches first.
        final Resource[] resources = new PathMatchingResourcePatternResolver().getResources(SPEC_RESOURCES);
        final List<String> titlesFound = new ArrayList<>();
        for (final Resource resource : resources) {
            try (InputStream stream = resource.getInputStream()) {
                final Map<String, Object> spec = new Yaml().load(stream);
                final String title = String.valueOf(((Map<String, Object>) spec.get("info")).get("title"));
                titlesFound.add(title);
                if (SPEC_TITLE.equals(title)) {
                    return new ArrayList<>(((Map<String, Object>) spec.get("paths")).keySet());
                }
            }
        }
        return fail("no OpenAPI spec titled '%s' on the classpath; titles found: %s", SPEC_TITLE, titlesFound);
    }

    @Test
    @DisplayName("The exempt list carries no contract path and no endpoint that could hold case data")
    void exempt_paths_are_infrastructure_only() {
        assertThat(policy.exemptPaths()).noneMatch(path -> path.startsWith("/courthouses"));
    }
}
