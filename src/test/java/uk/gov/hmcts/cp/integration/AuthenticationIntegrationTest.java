package uk.gov.hmcts.cp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.clients.CourtHousesClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.security.TestTokens.appOnlyClaims;
import static uk.gov.hmcts.cp.security.TestTokens.bearer;
import static uk.gov.hmcts.cp.security.TestTokens.validBearer;

class AuthenticationIntegrationTest extends IntegrationTestBase {

    private static final UUID COURT_ID = UUID.fromString("57323172-1083-454d-8ab7-2455a8b993e7");
    private static final String COURT_URL = "/courthouses/" + COURT_ID;

    @Autowired
    private CourtHousesClient client;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("A protected endpoint accepts a token minted by the test key set")
    void accepts_token_minted_by_the_in_process_key_set() throws Exception {
        stubBackend();

        mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, validBearer()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courtHouseType").value("magistrate"));
    }

    @Test
    @DisplayName("A protected endpoint rejects a request with no token")
    void rejects_request_without_token() throws Exception {
        mockMvc.perform(get(COURT_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(WWW_AUTHENTICATE, org.hamcrest.Matchers.containsString("error=\"invalid_token\"")))
            .andExpect(jsonPath("$.message").value("MISSING_AUTHORIZATION_HEADER"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    @DisplayName("A protected endpoint rejects an invalid token")
    void rejects_request_with_invalid_token() throws Exception {
        mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("MALFORMED_TOKEN"));
    }

    @Test
    @DisplayName("A token without a recognised role is a 403, not a 401")
    void rejects_token_without_recognised_role_as_forbidden() throws Exception {
        final String token = bearer(appOnlyClaims().claim("roles", List.of("Some.Other.Role")).build());

        mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, token))
            .andExpect(status().isForbidden())
            .andExpect(header().string(WWW_AUTHENTICATE, org.hamcrest.Matchers.containsString("error=\"insufficient_scope\"")))
            .andExpect(jsonPath("$.message").value("MISSING_ROLE"));
    }

    @Test
    @DisplayName("An expired token is rejected through the real filter chain")
    void rejects_expired_token_through_the_filter_chain() throws Exception {
        final String token = bearer(appOnlyClaims()
            .expirationTime(Date.from(Instant.now().minusSeconds(7200)))
            .build());

        mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("EXPIRED"));
    }

    @Test
    @DisplayName("Exempt infrastructure endpoints answer without a token")
    void exempt_endpoints_answer_without_a_token() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("A near-miss of an exempt path still requires a token")
    void near_miss_of_exempt_path_requires_a_token() throws Exception {
        mockMvc.perform(get("/healthx")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TracingFilter still runs first, so a rejection is traceable")
    void rejection_still_carries_the_trace_id_from_the_request() throws Exception {
        // TracingFilter sets the response header before delegating, so the header is only present
        // on a 401 if it ran ahead of the auth filter.
        mockMvc.perform(get(COURT_URL).header("traceId", "1234-1234"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("traceId", "1234-1234"));
    }

    @Test
    @DisplayName("The rejection response never echoes the token")
    void rejection_response_never_echoes_the_token() throws Exception {
        final String token = validBearer().substring("Bearer ".length());
        final String tampered = token.substring(0, token.length() - 4) + "AAAA";

        final String body = mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, "Bearer " + tampered))
            .andExpect(status().isUnauthorized())
            .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(body).doesNotContain(tampered);
    }

    private void stubBackend() throws Exception {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/courtHouseResponse.json"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(client.getRequestEntity()), eq(String.class)))
            .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
    }
}
