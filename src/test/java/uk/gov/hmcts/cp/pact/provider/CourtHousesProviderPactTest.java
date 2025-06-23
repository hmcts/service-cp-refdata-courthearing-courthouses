package uk.gov.hmcts.cp.pact.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.VenueContact;
import uk.gov.hmcts.cp.repositories.CourtHousesRepository;


import static java.util.Arrays.asList;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")  // important: use the in-memory repo!
@ExtendWith({SpringExtension.class, PactVerificationInvocationContextProvider.class})
@Provider("VPCourtHousePactProvider")
@PactBroker(
        scheme = "https",
        host = "hmcts-dts.pactflow.io",
        providerBranch = "dev/pactTest",
        authentication = @PactBrokerAuth(token = "eOmnLAeYytphFMQZIj7hUg")
)
public class CourtHousesProviderPactTest {

    @Autowired
    private CourtHousesRepository courtHousesRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupTarget(PactVerificationContext context) {
        System.out.println("Running test on port: " + port);
        context.setTarget(new HttpTestTarget("localhost", port));
        System.out.println("pact.verifier.publishResults: " + System.getProperty("pact.verifier.publishResults"));
    }

    @State("court house with ID 123 exists")
    public void setupCourtHouse() {
        courtHousesRepository.clearAll();
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

        final CourtHouseResponse courtHouseResponse = CourtHouseResponse.builder()
            .courtHouseType(CourtHouseResponse.CourtHouseTypeEnum.CROWN)
            .courtHouseCode("LND001")
            .courtHouseName("Central London County Court")
            .courtHouseDescription("Main Crown Court in London handling major cases")
            .courtRoom(asList(courtRoom))
            .build();

        courtHousesRepository.saveCourtHouse("23457", courtHouseResponse);
    }

    @TestTemplate
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
