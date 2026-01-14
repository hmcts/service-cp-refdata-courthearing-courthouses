package uk.gov.hmcts.cp.clients;

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
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CourtHousesClientTest {

    @Mock
    AppPropertiesBackend appProperties;
    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    private CourtHousesClient courtHousesClient;

    String courtId = UUID.randomUUID().toString();
    String courtRoomId = UUID.randomUUID().toString();
    String backendRoot = "http://localhost";
    String backendPath = "/referencedata-service/query/api/rest/referencedata/courtrooms";

    @Test
    void getCourtScheduleByCaseUrn_shouldReturnCourtScheduleResponse() {
        when(appProperties.getBackendUrl()).thenReturn(backendRoot);
        when(appProperties.getBackendPath()).thenReturn(backendPath);
        mockResponse();

        CourtHouseResponse response = courtHousesClient.getCourtHouse(courtId.toString(), courtRoomId.toString());

        assertThat(response.getCourtHouseCode()).isEqualTo("oucode");
        assertEquals(1, response.getCourtRoom().size());
    }

    private void mockResponse() {
        String expectedUrl = String.format("%s%s/%s", backendRoot, backendPath, courtId);
        HttpEntity<String> requestEntity = courtHousesClient.getRequestEntity();
        CourtResponse courtResponse = CourtResponse.builder()
            .oucode("oucode")
            .oucodeL1Name("Magistrates")
            .address1("address1")
            .courtrooms(List.of(CourtResponse.CourtRoom.builder().id(courtRoomId).courtroomId("21").build()))
            .build();
        ResponseEntity<CourtResponse> mockResponse = new ResponseEntity<>(courtResponse, HttpStatus.OK);
        log.info("Mocking {}", expectedUrl);
        when(restTemplate.exchange(
            eq(expectedUrl),
            eq(HttpMethod.GET),
            eq(requestEntity),
            eq(CourtResponse.class)
        )).thenReturn(mockResponse);
    }
}
