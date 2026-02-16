package uk.gov.hmcts.cp.clients;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.mappers.CourtHouseMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CourtHousesClientTest {

    @Mock
    AppPropertiesBackend appProperties;
    @Mock
    RestTemplate restTemplate;
    @Mock
    CourtHouseMapper mapper;

    @InjectMocks
    private CourtHousesClient courtHousesClient;

    UUID courtId = UUID.randomUUID();
    String backendRoot = "http://localhost";
    String backendPath = "/referencedata-service/query/api/rest/referencedata/courtrooms";

    @SneakyThrows
    @Test
    void getCourtScheduleByCaseUrn_shouldReturnCourtScheduleResponse() {
        when(appProperties.getBackendUrl()).thenReturn(backendRoot);
        when(appProperties.getBackendPath()).thenReturn(backendPath);
        String cpResponse = Files.readString(Path.of("src/test/resources/courtRoomResponse.json"));
        mockResponse(cpResponse);
        CourtResponse courtResponse = CourtResponse.builder().build();
        when(mapper.mapStringToCourtResponse(cpResponse)).thenReturn(courtResponse);

        CourtResponse response = courtHousesClient.getCourtHouse(courtId);

        assertThat(response).isEqualTo(courtResponse);
    }

    private void mockResponse(String response) {
        String expectedUrl = String.format("%s%s/%s", backendRoot, backendPath, courtId);
        HttpEntity<String> requestEntity = courtHousesClient.getRequestEntity();
        ResponseEntity<String> mockResponse = new ResponseEntity<>(response, HttpStatus.OK);
        log.info("Mocking {}", expectedUrl);
        when(restTemplate.exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            eq(requestEntity),
            eq(String.class)
        )).thenReturn(mockResponse);
    }
}
