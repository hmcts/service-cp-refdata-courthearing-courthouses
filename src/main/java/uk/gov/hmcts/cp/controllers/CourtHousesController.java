package uk.gov.hmcts.cp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.api.CourtHouseApi;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.services.CourtHousesService;

@RestController
@Slf4j
public class CourtHousesController implements CourtHouseApi {
    private static final Logger LOG = LoggerFactory.getLogger(CourtHousesController.class);
    private final CourtHousesService courtHousesService;

    public CourtHousesController(final CourtHousesService courtHousesService) {
        this.courtHousesService = courtHousesService;
    }

    @Override
    public ResponseEntity<CourtHouseResponse> getCourthouseByCourtIdAndCourtRoomId(final String courtId,
                                                                                   final String courtRoomId) {
        final String sanitizeCourtId;
        final String sanitizeCourtRoomId;
        final CourtHouseResponse courtHouseResponse;
        try {
            sanitizeCourtId = sanitizeCourtId(courtId);
            log.atInfo().log("courtId is : {} and courtRoomId : {} ", courtId, courtRoomId);
            sanitizeCourtRoomId = sanitizeCourtId(courtRoomId);
            courtHouseResponse = courtHousesService.getCourtHouse(sanitizeCourtId, sanitizeCourtRoomId);
            log.atInfo().log("courtId is : {} and courtRoomId : {} courtHouseCode is : {} ", courtId, courtRoomId,
                             courtHouseResponse.getCourtHouseCode());
        } catch (ResponseStatusException e) {
            LOG.atError().log(e.getMessage());
            throw e;
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(courtHouseResponse);
    }

    private String sanitizeCourtId(final String courtId) {
        if (courtId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtId is required");
        }
        return StringEscapeUtils.escapeHtml4(courtId);
    }
}
