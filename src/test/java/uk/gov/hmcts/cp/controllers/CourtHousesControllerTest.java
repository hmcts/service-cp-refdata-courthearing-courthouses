package uk.gov.hmcts.cp.controllers;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.services.CourtHousesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class CourtHousesControllerTest {

    @Mock
    private CourtHousesService courtHousesService;
    @InjectMocks
    private CourtHousesController courtHousesController;

    CourtHouseResponse response = CourtHouseResponse.builder().build();

    @Test
    void getCourthouseByCourtId_ShouldReturnResultsWithOkStatus() {
        String courtId = "123";
        String courtRoomId = "456";
        when(courtHousesService.getCourtHouse(courtId, courtRoomId)).thenReturn(response);
        log.info("Calling courtHousesController.getCourthouseByCourtId with courtId: {}", courtId);

        ResponseEntity<CourtHouseResponse> responseEntity = courtHousesController.getCourthouseByCourtIdAndCourtRoomId(
            courtId,
            courtRoomId
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).isEqualTo(response);
    }

    @Test
    void getCourthouseByCourtId_ShouldSanitizeCourtId() {
        String unsanitizedCourtId = "<script>alert('xss')</script>";
        String unsanitizedCourtRoomId = "<script>alert('xss')</script>";
        when(courtHousesService.getCourtHouse("&lt;script&gt;alert('xss')&lt;/script&gt;", "&lt;script&gt;alert('xss')&lt;/script&gt;")).thenReturn(response);
        ResponseEntity<CourtHouseResponse> response = courtHousesController.getCourthouseByCourtIdAndCourtRoomId(
            unsanitizedCourtId,
            unsanitizedCourtRoomId
        );
        assertNotNull(response);
        log.debug("Received response: {}", response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCourthouseByCourtId_ShouldReturnBadRequestStatus() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, () -> {
                courtHousesController.getCourthouseByCourtIdAndCourtRoomId(null, null);
            }
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("courtId is required");
        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"courtId is required\"");
    }
}
