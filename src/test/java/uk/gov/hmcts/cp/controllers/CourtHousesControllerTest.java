package uk.gov.hmcts.cp.controllers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.services.CourtHousesService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtHousesControllerTest {

    @Mock
    private CourtHousesService courtHousesService;
    @InjectMocks
    private CourtHousesController courtHousesController;
    @InjectMocks
    private CourtRoomsController courtRoomsController;

    UUID courtId = UUID.fromString("cfd36d18-4e36-4581-91a7-356539a2eb4d");
    UUID courtRoomId = UUID.fromString("71573215-31eb-47ca-b9b4-dadf314a5e21");
    CourtHouseResponse response = CourtHouseResponse.builder().build();

    @Test
    void getCourthouseByCourtIdShouldReturnResultsWithOkStatus() {
        when(courtHousesService.getCourtHouseByCourtId(courtId)).thenReturn(response);

        ResponseEntity<CourtHouseResponse> responseEntity = courtHousesController.getCourthouseByCourtId(courtId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isEqualTo(response);
    }

    @Test
    void get_courthouse_by_court_id_and_court_room_id_should_return_results_with_ok_status() {
        when(courtHousesService.getCourthouseByCourtIdAndCourtRoomId(courtId, courtRoomId)).thenReturn(response);

        ResponseEntity<CourtHouseResponse> responseEntity = courtRoomsController.getCourthouseByCourtIdAndCourtRoomId(
            courtId,
            courtRoomId
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isEqualTo(response);
    }
}
