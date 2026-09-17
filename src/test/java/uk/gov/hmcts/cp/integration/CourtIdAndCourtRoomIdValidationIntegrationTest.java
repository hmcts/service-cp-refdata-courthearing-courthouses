package uk.gov.hmcts.cp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.cp.security.TestTokens.validBearer;

@Slf4j
class CourtIdAndCourtRoomIdValidationIntegrationTest extends IntegrationTestBase {

    String courtId = UUID.randomUUID().toString();
    String courtRoomId = UUID.randomUUID().toString();

    @Test
    void random_urn_should_throw_404() throws Exception {
        mockMvc.perform(get("/something-else", "").header(AUTHORIZATION, validBearer()))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No endpoint GET /something-else."));
    }

    @Test
    void empty_courtId_should_throw_404() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", "", courtRoomId);
        mockMvc.perform(get(url).header(AUTHORIZATION, validBearer()))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("No endpoint GET /courthouses/")));
    }

    @Test
    void non_uuid_courtId_should_throw_400() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", "not-a-uuid", courtRoomId);
        mockMvc.perform(get(url).header(AUTHORIZATION, validBearer()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message",
                                containsString(
                                    "Method parameter 'court_id': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'")
            ));
    }

    @Test
    void empty_courtRoomId_should_throw_404() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", courtId, "");
        mockMvc.perform(get(url).header(AUTHORIZATION, validBearer()))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("No endpoint GET /courthouses/")));
    }

    @Test
    void non_uuid_courtRoomId_should_throw_400() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", courtId, "not-a-uuid");
        String expectedError = "Method parameter 'court_room_id': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'";
        mockMvc.perform(get(url).header(AUTHORIZATION, validBearer()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString(expectedError)));
    }
}
