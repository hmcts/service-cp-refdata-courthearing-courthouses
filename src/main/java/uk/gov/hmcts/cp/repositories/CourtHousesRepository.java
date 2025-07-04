package uk.gov.hmcts.cp.repositories;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

@Repository
public interface CourtHousesRepository {
    CourtHouseResponse getCourtHouse(String courtId);
    void saveCourtHouse(String caseUrn, CourtHouseResponse courtHouseResponse);
    void clearAll();
}
