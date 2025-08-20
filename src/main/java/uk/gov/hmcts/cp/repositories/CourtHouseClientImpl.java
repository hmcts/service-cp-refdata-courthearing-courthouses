package uk.gov.hmcts.cp.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static uk.gov.hmcts.cp.utils.Utils.getHttpClient;

@Component
@Primary
@RequiredArgsConstructor
@Profile("!pact-test")
public class CourtHouseClientImpl implements CourtHousesClient {
    private static final Logger LOG = LoggerFactory.getLogger(CourtHouseClientImpl.class);
    private final HttpClient httpClient;

    @Value("${service.court-house-client.url}")
    private String courtHouseClientUrl;

    @Value("${service.court-house-client.cjscppuid}")
    private String cjscppuid;

    public CourtHouseClientImpl() throws NoSuchAlgorithmException, KeyManagementException {
        this.httpClient = getHttpClient();
    }

    public CourtHouseClientImpl(HttpClient httpClient,
                                @Value("${service.court-house-client.url}") String courtHouseClientUrl,
                                @Value("${service.court-house-client.cjscppuid}") String cjscppuid) {
        this.httpClient = httpClient;
        this.courtHouseClientUrl = courtHouseClientUrl;
        this.cjscppuid = cjscppuid;
    }

    public String getCourtHouseClientUrl() {
        LOG.info("courtHouseClientUrl is : {}", this.courtHouseClientUrl);
        //return "https://steccm64.ingress01.dev.nl.cjscp.org.uk/referencedata-service/query/api/rest/referencedata/organisation-units" ;
        return "https://steccm64.ingress01.dev.nl.cjscp.org.uk/referencedata-service/query/api/rest/referencedata/courtrooms";
    }

    public String getCjscppuid() {
        LOG.info("cjscppuid is : {}", this.cjscppuid);
        return "d7c91866-646a-462c-9203-46678e8cddef" ;
    }

    @Override
    public CourtHouseResponse getCourtHouse(String courtId, String courtRoomId) {
        CourtResponse courtResponse = getCourtHouseAndRoomDetails(courtId);

        CourtResponse.CourtRoom cr = courtResponse.getCourtrooms()
            .stream()
            .filter(a -> a.getId().equals(courtRoomId))
            .findFirst()
            .orElse(null);

//        final VenueContact venueContact = VenueContact.builder()
//            .venueTelephone("01772 844700") //phone
//            .venueEmail("court1@moj.gov.uk") //email
//            .primaryContactName("Name")
//            .venueSupport("0330 566 5561")
//            .build();

        final Address address = Address.builder()
            .address1(courtResponse.getAddress1()) //address1
            .address2(courtResponse.getAddress2()) //address2
            .address3(courtResponse.getAddress3()) //address3
            .address4(courtResponse.getAddress4()) //address4 + address5
            .postalCode(courtResponse.getPostcode())//postcode
            .country("UK")
            .build();

        final CourtRoom courtRoom = CourtRoom.builder()
            //.courtRoomNumber(1)
            .courtRoomId(Integer.valueOf(cr.getCourtroomId())) //courtId
            .courtRoomName(cr.getCourtroomName())
            //.venueContact(venueContact)
            .address(address)
            .build();

        return CourtHouseResponse.builder()
            .courtHouseType(getCourtHouseType(courtResponse)) //oucodeL1Name
            .courtHouseCode(courtResponse.getOucode()) //oucode
            .courtHouseName(courtResponse.getOucodeL3Name()) //oucodeL3Name
           // .courtHouseDescription("Main Crown Court in London handling major cases")
            .courtRoom(Arrays.asList(courtRoom))
            .build();
    }

    private CourtHouseResponse.CourtHouseTypeEnum getCourtHouseType(CourtResponse courtResponse) {
        return courtResponse.getOucodeL1Name().contains("Magistrates") ?
            CourtHouseResponse.CourtHouseTypeEnum.MAGISTRATE :
            CourtHouseResponse.CourtHouseTypeEnum.CROWN;
    }

    private CourtResponse getCourtHouseAndRoomDetails(String courtId){
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(buildUrl(courtId)))
                    .GET()
                    .header("Accept", "application/vnd.referencedata.ou-courtroom+json")
                    .header("CJSCPPUID", getCjscppuid())
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                LOG.error("Failed to fetch OU data. HTTP Status: {}", response.statusCode());
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            CourtResponse courtResponse = objectMapper.readValue(
                    response.body(),
                    CourtResponse.class
            );
            LOG.info("Response Code: {}, Response Body: {}", response.statusCode(), response.body());
            return courtResponse;
        } catch (Exception e) {
            LOG.error("Exception occurred while fetching court room data: {}", e.getMessage());
        }
        return null;
    }

    private String buildUrl(String courtId) {
        return UriComponentsBuilder
                .fromUri(URI.create(getCourtHouseClientUrl()))
                .pathSegment(courtId)
                .buildAndExpand(courtId)
                .toUriString();
    }
}
