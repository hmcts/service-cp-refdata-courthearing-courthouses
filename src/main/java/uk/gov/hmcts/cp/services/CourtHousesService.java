package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.clients.CourtHousesClient;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtHousesService {
    private final CourtHousesClient courtHousesClient;

    public CourtHouseResponse getCourtHouse(final String courtId,
                                            final String courtRoomId) {
        if (StringUtils.isEmpty(courtId) || StringUtils.isEmpty(courtRoomId)) {
            log.warn("No court id or court room id provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtId and court room id is required");
        }
        return courtHousesClient.getCourtHouse(courtId, courtRoomId);
    }
}
