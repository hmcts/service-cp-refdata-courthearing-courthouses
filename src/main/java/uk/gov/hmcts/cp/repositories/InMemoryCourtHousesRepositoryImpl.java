package uk.gov.hmcts.cp.repositories;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.VenueContact;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCourtHousesRepositoryImpl implements CourtHousesRepository {

    private final Map<String, CourtHouseResponse> CourtHouseResponseMap = new ConcurrentHashMap<>();

    public void saveCourtHouse(final String courtId, final CourtHouseResponse courtHouseResponse){
        CourtHouseResponseMap.put(courtId, courtHouseResponse);
    }

    public CourtHouseResponse getCourtHouse(final String courtId) {
        if (!CourtHouseResponseMap.containsKey(courtId)) {
            saveCourtHouse(courtId, createCourtHouseResponse());
        }
        return CourtHouseResponseMap.get(courtId);
    }

    public void clearAll(){
        CourtHouseResponseMap.clear();
    }

    private CourtHouseResponse createCourtHouseResponse(){
        final VenueContact venueContact = VenueContact.builder()
            .venueTelephone("01772 844700")
            .venueEmail("court1@moj.gov.uk")
            .primaryContactName("Name")
            .venueSupport("0330 566 5561")
            .build();

        final Address address = Address.builder()
            .address1("Thomas More Building")
            .address2("Royal Courts of Justice")
            .address3("Strand")
            .address4("London")
            .postalCode("WC2A 2LL")
            .country("UK")
            .build();

        final CourtRoom courtRoom = CourtRoom.builder()
            .courtRoomNumber(1)
            .courtRoomId(1001)
            .courtRoomName("Courtroom 1")
            .venueContact(venueContact)
            .address(address)
            .build();

        return CourtHouseResponse.builder()
            .courtHouseType(CourtHouseResponse.CourtHouseTypeEnum.CROWN)
            .courtHouseCode("LND001")
            .courtHouseName("Central London County Court")
            .courtHouseDescription("Main Crown Court in London handling major cases")
            .courtRoom(Arrays.asList(courtRoom))
            .build();
    }
}
