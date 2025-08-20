package uk.gov.hmcts.cp.controllers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;
import uk.gov.hmcts.cp.repositories.CourtHousesClient;
import uk.gov.hmcts.cp.repositories.InMemoryCourtHousesClientImpl;
import uk.gov.hmcts.cp.services.CourtHousesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourtHousesControllerTest {
    private static final Logger log = LoggerFactory.getLogger(CourtHousesControllerTest.class);

    private CourtHousesClient courtHousesClient;
    private CourtHousesService courtHousesService;
    private CourtHousesController courtHousesController;

    @BeforeEach
    void setUp() {
        courtHousesClient = new InMemoryCourtHousesClientImpl();
        courtHousesService = new CourtHousesService(courtHousesClient);
        courtHousesController = new CourtHousesController(courtHousesService);
    }

    @Test
    void getCourthouseByCourtId_ShouldReturnResultsWithOkStatus() {
        String courtId = "123";
        String courtRoomId = "123";
        log.info("Calling courtHousesController.getCourthouseByCourtId with courtId: {}", courtId);
        ResponseEntity<CourtHouseResponse> response = courtHousesController.getCourthouseByCourtIdAndCourtRoomId(courtId, courtRoomId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CourtHouseResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Central London County Court", responseBody.getCourtHouseName());
        assertEquals("Main Crown Court in London handling major cases", responseBody.getCourtHouseDescription());
        CourtRoom courtRoom = responseBody.getCourtRoom().get(0);
        assertNotNull(courtRoom);
        assertEquals("Courtroom 1", courtRoom.getCourtRoomName());
        log.debug("Received CourtHouseResponse: {}", responseBody);
    }

    @Test
    void getCourthouseByCourtId_ShouldSanitizeCourtId() {
        String unsanitizedCourtId = "<script>alert('xss')</script>";
        String unsanitizedCourtRoomId = "<script>alert('xss')</script>";
        log.info("Calling courtHousesController.getCourthouseByCourtId with unsanitized courtId: {}", unsanitizedCourtId);

        ResponseEntity<CourtHouseResponse> response = courtHousesController.getCourthouseByCourtIdAndCourtRoomId(unsanitizedCourtId,unsanitizedCourtRoomId);
        assertNotNull(response);
        log.debug("Received response: {}", response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCourthouseByCourtId_ShouldReturnBadRequestStatus() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            courtHousesController.getCourthouseByCourtIdAndCourtRoomId(null, null);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("courtId is required");
        assertThat(exception.getMessage()).isEqualTo("400 BAD_REQUEST \"courtId is required\"");
    }
}
