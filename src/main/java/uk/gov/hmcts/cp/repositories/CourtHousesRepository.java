package uk.gov.hmcts.cp.repositories;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

@Repository
@FunctionalInterface
public interface CourtHousesRepository {
    CourtHouseResponse getCourtHouse(final String courtId);
}
