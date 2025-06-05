package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.repositories.CourtHousesRepository;

@Service
@RequiredArgsConstructor
public class CourtHousesService {
    private static final Logger LOG = LoggerFactory.getLogger(CourtHousesService.class);
    private final CourtHousesRepository courtHousesRepository;

    public CourtHouseResponse getCourtHouse(final String courtId) {
        if (StringUtils.isEmpty(courtId)) {
            LOG.warn("No court id provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtId is required");
        }
        LOG.warn("NOTE: System configured to return stubbed court house details. Ignoring provided courtId : {}", courtId);
        final CourtHouseResponse  stubbedcourtHouseResponse = courtHousesRepository.getCourtHouse(courtId);
        LOG.debug("Court House response: {}", stubbedcourtHouseResponse);
        return stubbedcourtHouseResponse;
    }
}
