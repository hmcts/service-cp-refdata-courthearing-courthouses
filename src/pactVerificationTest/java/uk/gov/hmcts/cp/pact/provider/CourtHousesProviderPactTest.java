package uk.gov.hmcts.cp.pact.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.pact.helper.JsonFileToObject;
import uk.gov.hmcts.cp.repositories.CourtHousesClient;
import uk.gov.hmcts.cp.repositories.InMemoryCourtHousesClientImpl;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SpringExtension.class, PactVerificationInvocationContextProvider.class})
@Provider("CPRefDataCourtHouseProvider")
@PactBroker(
    url = "${pact.broker.url}",
    authentication = @PactBrokerAuth(token = "${pact.broker.token}")
)
@TestPropertySource(properties = {
    "service.court-house-client.url=https://CAOURT_HOUSE.org.uk",
    "service.court-house-client.cjscppuid=MOCK-CJSCPPUID"
})
@ActiveProfiles("pact-test")
@Tag("pact")
public class CourtHousesProviderPactTest {

    private static final Logger LOG = LoggerFactory.getLogger(CourtHousesProviderPactTest.class);

    @Autowired
    private InMemoryCourtHousesClientImpl inMemoryCourtHousesClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setupTarget(PactVerificationContext context) {
        log.debug("Running test on port: " + port);
        context.setTarget(new HttpTestTarget("localhost", port));
        log.debug("pact.verifier.publishResults: " + System.getProperty("pact.verifier.publishResults"));
    }

    @State("court house with ID 123 exists")
    public void setupCourtHouse() throws Exception{
        inMemoryCourtHousesClient.clearAll();
        CourtHouseResponse courtHouseResponse =  JsonFileToObject.readJsonFromResources("courtHouse.json", CourtHouseResponse.class);
        inMemoryCourtHousesClient.saveCourtHouse("23457", courtHouseResponse);
    }

    @TestTemplate
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
