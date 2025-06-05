package uk.gov.hmcts.cp.repositories;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponseCourtRoomInner;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponseCourtRoomInnerAddress;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponseCourtRoomInnerVenueContact;

import java.util.Arrays;

@Component
public class InMemoryCourtHousesRepositoryImpl implements CourtHousesRepository {

    public CourtHouseResponse getCourtHouse(final String courtId) {
        final CourtHouseResponseCourtRoomInnerVenueContact venueContact = CourtHouseResponseCourtRoomInnerVenueContact.builder()
            .venueTelephone("01772 844700")
            .venueEmail("court1@moj.gov.uk")
            .primaryContactName("Name")
            .venueSupport("0330 566 5561")
            .build();

        final CourtHouseResponseCourtRoomInnerAddress address = CourtHouseResponseCourtRoomInnerAddress.builder()
            .address1("Thomas More Building")
            .address2("Royal Courts of Justice")
            .address3("Strand")
            .address4("London")
            .postalCode("WC2A 2LL")
            .country("UK")
            .build();

        final CourtHouseResponseCourtRoomInner courtRoom = CourtHouseResponseCourtRoomInner.builder()
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
