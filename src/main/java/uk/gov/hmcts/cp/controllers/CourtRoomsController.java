package uk.gov.hmcts.cp.controllers;

import uk.gov.hmcts.cp.openapi.api.CourtRoomApi;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.services.CourtHousesService;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CourtRoomsController implements CourtRoomApi {
    private final CourtHousesService courtHousesService;

    public CourtRoomsController(final CourtHousesService courtHousesService) {
        this.courtHousesService = courtHousesService;
    }

    @Override
    public ResponseEntity<CourtHouseResponse> getCourthouseByCourtIdAndCourtRoomId(final UUID courtId,
                                                                                   final UUID courtRoomId) {
        log.info("courtId is : {} and courtRoomId : {} ", courtId, courtRoomId);
        final CourtHouseResponse courtHouseResponse = courtHousesService.getCourthouseByCourtIdAndCourtRoomId(courtId, courtRoomId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(courtHouseResponse);
    }
}
