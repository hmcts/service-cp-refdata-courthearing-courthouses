package uk.gov.hmcts.cp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "service.court-house-client.url=https://CAOURT_HOUSE.org.uk",
    "service.court-house-client.cjscppuid=MOCK-CJSCPPUID"
})
class RootControllerIntegrationTest extends IntegrationTestBase {

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    void shouldCallRootAndGet200() throws Exception {

        final MvcResult result = mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome to service-cp-refdata-courthearing-courthouses")))
            .andReturn();
        assertThat(result).isNotNull();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @DisplayName("Actuator health status should be UP")
    @Test
    void shouldCallActuatorAndGet200() throws Exception {
        final MvcResult result = mockMvc.perform(get("/health"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andReturn();
        assertThat(result).isNotNull();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
