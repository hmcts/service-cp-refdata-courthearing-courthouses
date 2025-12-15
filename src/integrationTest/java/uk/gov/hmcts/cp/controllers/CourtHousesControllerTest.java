package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cp.config.IntegrationTestConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
@TestPropertySource(properties = {
    "service.court-house-client.url=https://CAOURT_HOUSE.org.uk",
    "service.court-house-client.cjscppuid=MOCK-CJSCPPUID"
})
class CourtHousesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("Should /courthouses/{court_id}/courtrooms/{court_room_id} request with 200 response code")
    @Test
    void shouldCallActuatorAndGet200() throws Exception {
        final MvcResult result = mockMvc.perform(get("/courthouses/123/courtrooms/123"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result).isNotNull();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
