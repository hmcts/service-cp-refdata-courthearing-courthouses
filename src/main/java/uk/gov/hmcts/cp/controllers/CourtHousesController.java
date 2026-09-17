package uk.gov.hmcts.cp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.cp.openapi.api.CourtHouseApi;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.services.CourtHousesService;

import java.util.UUID;

@RestController
@Slf4j
public class CourtHousesController implements CourtHouseApi {
    private final CourtHousesService courtHousesService;

    public CourtHousesController(final CourtHousesService courtHousesService) {
        this.courtHousesService = courtHousesService;
    }

    @Override
    public ResponseEntity<CourtHouseResponse> getCourthouseByCourtId(final UUID courtId) {
        log.info("courtId is : {}", courtId);
        final CourtHouseResponse courtHouseResponse = courtHousesService.getCourtHouseByCourtId(courtId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(courtHouseResponse);
    }
}
