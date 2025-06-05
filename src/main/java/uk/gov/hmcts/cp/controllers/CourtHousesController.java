package uk.gov.hmcts.cp.controllers;

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
public class CourtHousesController implements CourtHouseApi {
    private static final Logger LOG = LoggerFactory.getLogger(CourtHousesController.class);
    private final CourtHousesService courtHousesService;

    public CourtHousesController(final CourtHousesService courtHousesService) {
        this.courtHousesService = courtHousesService;
    }

    @Override
    public ResponseEntity<CourtHouseResponse> getCourthouseByCourtId(final String courtId) {
        String sanitizeCourtId = "";
        CourtHouseResponse courtHouseResponse = null;
        try {
            sanitizeCourtId = sanitizeCourtId(courtId);
            courtHouseResponse = courtHousesService.getCourtHouse(courtId);
        } catch (ResponseStatusException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessage());
                throw e;
            }
        }
        LOG.debug("Found Court House response for caseId: {}", sanitizeCourtId);
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
