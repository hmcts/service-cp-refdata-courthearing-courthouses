package uk.gov.hmcts.cp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.clients.CourtHousesClient;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.mappers.CourtHouseMapper;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtHousesService {

    private final CourtHousesClient courtHousesClient;
    private final CourtHouseMapper courtHouseMapper;

    public CourtHouseResponse getCourthouseByCourtIdAndCourtRoomId(final UUID courtId, final UUID courtRoomId) {
        final CourtResponse courtResponse = courtHousesClient.getCourtHouse(courtId);
        return courtHouseMapper.mapCourtHouseCPResponseWithCourtRoomId(courtResponse, courtRoomId);
    }

    public CourtHouseResponse getCourtHouseByCourtId(final UUID courtId) {
        final CourtResponse courtResponse = courtHousesClient.getCourtHouse(courtId);
        return courtHouseMapper.mapCPResponseToCourtHouse(courtResponse);
    }
}
