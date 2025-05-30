package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.openapi.api.CourtHouseApi;
import uk.gov.hmcts.cp.openapi.model.CourtHouse;
import uk.gov.hmcts.cp.services.CourtHousesService;

@RestController
@RequiredArgsConstructor
public class CourtHousesController implements CourtHouseApi {

    private final CourtHousesService courtHousesService;

    @Override
    public ResponseEntity<CourtHouse> getCourthouseByCourtId(String courtId) {
        CourtHouse courtHouse = courtHousesService.getCourtHouse(courtId);
        return new ResponseEntity<>(courtHouse, HttpStatus.OK);
    }

}
