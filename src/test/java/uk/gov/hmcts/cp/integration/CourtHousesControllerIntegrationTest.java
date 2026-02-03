package uk.gov.hmcts.cp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.clients.CourtHousesClient;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class CourtHousesControllerIntegrationTest {

    @Autowired
    AppPropertiesBackend appProperties;
    @Autowired
    CourtHousesClient client;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    RestTemplate restTemplate;

    UUID courtId = UUID.randomUUID();
    UUID courtRoomId = UUID.fromString("a102458c-301f-3fe5-88d0-5cda9455f235");
    String url = String.format("/courthouses/%s/courtrooms/%s", courtId, courtRoomId);

    @Test
    void get_courthouses_should_return_ok() throws Exception {
        String jsonResponse = Files.readString(Path.of("src/test/resources/courtRoomResponse.json"));
        mockRestResponse(HttpStatus.OK, jsonResponse, courtId);
        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courtHouseType").value("magistrate"));
    }

    @Test
    void not_exist_thrown_should_throw_404() throws Exception {
        String expectedUrl = expectedUrl(courtId);
        log.info("Mocking {}", expectedUrl);
        when(restTemplate.exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            eq(client.getRequestEntity()),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("404 NOT_FOUND"));
    }

    @Test
    void not_exist_empty_should_throw_404() throws Exception {
        String expectedUrl = expectedUrl(courtId);
        log.info("Mocking {}", expectedUrl);
        mockRestResponse(HttpStatus.OK, "{}", courtId);
        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("404 NOT_FOUND"));
    }

    @Test
    void certificate_error_should_return_500() throws Exception {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            eq(client.getRequestEntity()),
            eq(String.class)
        )).thenThrow(new RuntimeException("SSL certificate problem: unable to get local issuer certificate"));
        mockMvc.perform(get(url))
            .andDo(print())
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.message").value("SSL certificate problem: unable to get local issuer certificate"));
    }

    private void mockRestResponse(HttpStatus httpStatus, String courtResponse, UUID courtRoomId) {
        String expectedUrl = expectedUrl(courtRoomId);
        log.info("Mocking {}", expectedUrl);
        when(restTemplate.exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            eq(client.getRequestEntity()),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>(courtResponse, httpStatus));
    }

    private String expectedUrl(UUID courtRoomId) {
        return String.format(
            "%s%s/%s",
            appProperties.getBackendUrl(),
            appProperties.getBackendPath(),
            courtRoomId
        );
    }
}
