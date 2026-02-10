package uk.gov.hmcts.cp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    String courtId = UUID.randomUUID().toString();
    String courtRoomId = UUID.randomUUID().toString();

    @Test
    void random_urn_should_throw_404() throws Exception {
        mockMvc.perform(get("/something-else", ""))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No endpoint GET /something-else."));
    }

    @Test
    void non_uuid_courtId_should_throw_404() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", "", courtRoomId);
        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("No endpoint GET /courthouses/")));
    }

    @Test
    void empty_courtRoom_should_throw_404() throws Exception {
        String url = String.format("/courthouses/%s/courtrooms/%s", courtId, "");
        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("No endpoint GET /courthouses/")));
    }
}
