package uk.gov.hmcts.cp.repositories;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.VenueContact;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("inMemoryCourtHousesClientImpl")
@Profile("pact-test")
public class InMemoryCourtHousesClientImpl implements CourtHousesClient {
    private final Map<String, CourtHouseResponse> courtHouseResponseMap = new ConcurrentHashMap<>();

    public void saveCourtHouse(final String courtId, final CourtHouseResponse courtHouseResponse){
        courtHouseResponseMap.put(courtId, courtHouseResponse);
    }

    public CourtHouseResponse getCourtHouse(final String courtId, final String courtRoomId) {
        if (!courtHouseResponseMap.containsKey(courtId)) {
            saveCourtHouse(courtId, createCourtHouseResponse());
        }
        return courtHouseResponseMap.get(courtId);
    }

    public void clearAll(){
        courtHouseResponseMap.clear();
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
            .courtRoomId(1001)
            .courtRoomName("Courtroom 1")
            .build();

        return CourtHouseResponse.builder()
            .courtHouseType(CourtHouseResponse.CourtHouseTypeEnum.CROWN)
            .courtHouseCode("LND001")
            .courtHouseName("Central London County Court")
            .address(address)
            .courtRoom(Arrays.asList(courtRoom))
            .build();
    }
}
