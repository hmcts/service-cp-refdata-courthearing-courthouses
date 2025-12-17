package uk.gov.hmcts.cp.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "service.court-house-client.url=http://court-house.org.uk",
    "service.court-house-client.path=/referencedata-service/query/api/rest/referencedata/courtrooms",
    "service.court-house-client.cjscppuid=MOCK-CJSCPPUID"
})
@Slf4j
public class ColinCourtHouseIntegrationTest {
    @MockitoBean
    HttpClient httpClient;
    @MockitoBean
    HttpResponse<String> mockResponse;
    @Resource
    protected MockMvc mockMvc;

    @Test
    void get_court_house_should_return_ok() throws Exception {
        when(httpClient.send(
            any(HttpRequest.class),
            eq(HttpResponse.BodyHandlers.ofString())
        )).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("xxx");
        mockMvc.perform(get("/courthouses/{courtId}/courtrooms/{roomId}", 123, 123))
            .andDo(print())
            .andExpect(status().isOk());
    }
}
