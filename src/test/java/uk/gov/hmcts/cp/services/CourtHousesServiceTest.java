package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.clients.CourtHousesClient;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtHousesServiceTest {

    @Mock
    private CourtHousesClient courtHousesClient;

    @InjectMocks
    private CourtHousesService courtHousesService;

    CourtHouseResponse courtHouseResponse = CourtHouseResponse.builder().build();

    @Test
    void service_should_call_client() {
        String validCourtId = "123";
        String validCourtRoomId = "123";
        when(courtHousesClient.getCourtHouse(validCourtId, validCourtRoomId)).thenReturn(courtHouseResponse);

        CourtHouseResponse response = courtHousesService.getCourtHouse(validCourtId, validCourtRoomId);

        assertThat(response).isEqualTo(courtHouseResponse);
    }

    @Test
    void service_should_bad_request_when_null_courtId() {
        String nullCourtId = null;

        assertThatThrownBy(() -> courtHousesService.getCourtHouse(nullCourtId, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId and court room id is required");
    }

    @Test
    void service_should_bad_request_when_empty_courtId() {
        // Arrange
        String emptyCourtId = "";
        String emptyCourtRoomId = "";

        // Act & Assert
        assertThatThrownBy(() -> courtHousesService.getCourtHouse(emptyCourtId, emptyCourtRoomId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId and court room id is required");
    }
}
