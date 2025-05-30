package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.openapi.model.CourtHouse;
import uk.gov.hmcts.cp.openapi.model.CourtHouseCourtRoomInner;

import java.util.List;

@Service
public class CourtHousesService {

    public CourtHouse getCourtHouse(String courtId) {
        CourtHouse courtHouse = new CourtHouse();
        courtHouse.courtHouseCode(courtId);
        courtHouse.courtHouseName("House name 221B");
        courtHouse.courtHouseType(CourtHouse.CourtHouseTypeEnum.CROWN);
        courtHouse.courtHouseDescription("House name 221B description");
        CourtHouseCourtRoomInner houseCourtRoom = new CourtHouseCourtRoomInner();
        houseCourtRoom.courtRoomId(1);
        houseCourtRoom.courtRoomNumber(1);
        houseCourtRoom.courtRoomName("Room 1");
        courtHouse.courtRoom(List.of(houseCourtRoom));
        return courtHouse;
    }
}
