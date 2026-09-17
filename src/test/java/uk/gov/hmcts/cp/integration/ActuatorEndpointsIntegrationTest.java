package uk.gov.hmcts.cp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.clients.CourtHousesClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.security.TestTokens.validBearer;

class ActuatorEndpointsIntegrationTest extends IntegrationTestBase {

    private static final String COURT_URL = "/courthouses/57323172-1083-454d-8ab7-2455a8b993e7";

    @Autowired
    private CourtHousesClient client;

    @MockitoBean
    private RestTemplate restTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"/health", "/info", "/prometheus"})
    @DisplayName("Exposed actuator endpoints answer without a token")
    void actuator_endpoints_answer_without_a_token(final String path) throws Exception {
        // Probes and scrapers send no Authorization header, so every exposed endpoint has to answer
        // without one even though auth.mode is ENFORCE here.
        mockMvc.perform(get(path)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Info reports the build and git details this service was assembled from")
    void info_reports_build_and_git_details() throws Exception {
        mockMvc.perform(get("/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.build.name").value("service-cp-refdata-courthearing-courthouses"))
            .andExpect(jsonPath("$.build.version").exists())
            .andExpect(jsonPath("$.git.commit.id").exists());
    }

    @Test
    @DisplayName("Auth counters are actually scrapeable, not merely registered")
    void auth_counters_are_scrapeable() throws Exception {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/courtHouseResponse.json"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(client.getRequestEntity()), eq(String.class)))
            .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        mockMvc.perform(get(COURT_URL).header(AUTHORIZATION, validBearer())).andExpect(status().isOk());
        mockMvc.perform(get(COURT_URL)).andExpect(status().isUnauthorized());

        final String scrape = mockMvc.perform(get("/prometheus"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(scrape)
            .contains("auth_token_validation_success_total")
            .contains("auth_token_validation_failure_total")
            .contains("MISSING_AUTHORIZATION_HEADER");
    }
}
