package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.clients.CourtHousesClient;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.mappers.CourtHouseMapper;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtHousesServiceTest {

    @Mock
    private CourtHousesClient courtHousesClient;
    @Mock
    private CourtHouseMapper mapper;

    @InjectMocks
    private CourtHousesService courtHousesService;

    CourtResponse courtResponse = CourtResponse.builder().build();
    UUID courtId = UUID.fromString("494d4085-4317-4153-b5a5-2d8918900275");
    UUID courtRoomId = UUID.fromString("2edc5ba7-1832-4f65-ae01-7f712c2e6ecd");
    CourtHouseResponse courtHouseResponse = CourtHouseResponse.builder().build();

    @Test
    void service_should_call_client() {
        when(courtHousesClient.getCourtHouse(courtId)).thenReturn(courtResponse);
        when(mapper.mapCommonPlatformResponse(courtResponse, courtRoomId)).thenReturn(courtHouseResponse);

        CourtHouseResponse response = courtHousesService.getCourtHouse(courtId, courtRoomId);

        assertThat(response).isEqualTo(courtHouseResponse);
    }
}
