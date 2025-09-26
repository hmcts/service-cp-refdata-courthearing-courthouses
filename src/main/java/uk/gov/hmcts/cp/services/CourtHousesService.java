package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.repositories.CourtHousesClient;

@Service
@RequiredArgsConstructor
public class CourtHousesService {
    private static final Logger LOG = LoggerFactory.getLogger(CourtHousesService.class);
    private final CourtHousesClient courtHousesClient;

    public CourtHouseResponse getCourtHouse(final String courtId,
                                            final String courtRoomId) {
        if (StringUtils.isEmpty(courtId) || StringUtils.isEmpty(courtRoomId)) {
            LOG.atWarn().log("No court id or court room id provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtId and court room id is required");
        }
        LOG.atWarn().log("NOTE: System configured to return stubbed court house details. Ignoring provided courtId: {} and court room id: {}", courtId, courtRoomId);
        return courtHousesClient.getCourtHouse(courtId, courtRoomId);
    }
}
