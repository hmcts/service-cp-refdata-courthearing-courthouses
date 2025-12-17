package uk.gov.hmcts.cp.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "service.court-house-client.url=https://CAOURT_HOUSE.org.uk",
    "service.court-house-client.cjscppuid=MOCK-CJSCPPUID"
})
@Slf4j
public class ColinCourtHouseIntegrationTest {
    @Resource
    protected MockMvc mockMvc;

    @Test
    void get_court_house_should_return_ok() throws Exception {
        // /courthouses/123/courtrooms/123
        mockMvc.perform(get("/courthouses/{courtId}/courtrooms/{roomId}", 123, 123))
            .andDo(print())
            .andExpect(status().isOk());
    }
}
