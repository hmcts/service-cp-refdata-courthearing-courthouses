package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.openapi.api.DefaultApi;
import uk.gov.hmcts.cp.openapi.model.CourtHousesschema;
import uk.gov.hmcts.cp.services.CourtHousesService;

@RestController
@RequiredArgsConstructor
public class CourtHousesController implements DefaultApi {

    private final CourtHousesService courtHousesService;

    @Override
    public ResponseEntity<CourtHousesschema> courthousesCourtIdGet(String courtId) {
        CourtHousesschema courtHousesschema = courtHousesService.getCourtHouse(courtId);
        return new ResponseEntity<>(courtHousesschema, HttpStatus.OK);
    }

}
