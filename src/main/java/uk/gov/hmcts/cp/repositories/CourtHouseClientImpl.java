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

    public CourtHouseClientImpl(final HttpClient httpClient,
                                @Value("${service.court-house-client.url}") final String courtHouseClientUrl,
                                @Value("${service.court-house-client.cjscppuid}") final String cjscppuid) {
        this.httpClient = httpClient;
        this.courtHouseClientUrl = courtHouseClientUrl;
        this.cjscppuid = cjscppuid;
    }

    public String getCourtHouseClientUrl() {
        LOG.info("courtHouseClientUrl is : {}", this.courtHouseClientUrl);
        return "https://steccm64.ingress01.dev.nl.cjscp.org.uk/referencedata-service/query/api/rest/referencedata/courtrooms";
    }

    public String getCjscppuid() {
        LOG.info("cjscppuid is : {}", this.cjscppuid);
        return "d7c91866-646a-462c-9203-46678e8cddef" ;
    }

    @Override
    public CourtHouseResponse getCourtHouse(final String courtId, final String courtRoomId) {
        final CourtResponse courtResponse = getCourtHouseAndRoomDetails(courtId);

        final CourtResponse.CourtRoom courtroom = courtResponse.getCourtrooms()
            .stream()
            .filter(a -> a.getId().equals(courtRoomId))
            .findFirst()
            .orElse(null);

        final Address address = Address.builder()
            .address1(courtResponse.getAddress1())
            .address2(courtResponse.getAddress2())
            .address3(courtResponse.getAddress3())
            .address4(courtResponse.getAddress4())
            .postalCode(courtResponse.getPostcode())
            .country("UK")
            .build();

        final CourtRoom courtRoom = CourtRoom.builder()
            .courtRoomId(Integer.valueOf(courtroom.getCourtroomId()))
            .courtRoomName(courtroom.getCourtroomName())
            .build();

        return CourtHouseResponse.builder()
            .courtHouseType(getCourtHouseType(courtResponse))
            .courtHouseCode(courtResponse.getOucode())
            .courtHouseName(courtResponse.getOucodeL3Name())
            .address(address)
            .courtRoom(Arrays.asList(courtRoom))
            .build();
    }

    private CourtHouseResponse.CourtHouseTypeEnum getCourtHouseType(final CourtResponse courtResponse) {
        return courtResponse.getOucodeL1Name().contains("Magistrates") ?
            CourtHouseResponse.CourtHouseTypeEnum.MAGISTRATE :
            CourtHouseResponse.CourtHouseTypeEnum.CROWN;
    }

    private CourtResponse getCourtHouseAndRoomDetails(final String courtId){
        CourtResponse courtResponse = null ;
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(buildUrl(courtId)))
                    .GET()
                    .header("Accept", "application/vnd.referencedata.ou-courtroom+json")
                    .header("CJSCPPUID", getCjscppuid())
                    .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.OK.value()) {
                final ObjectMapper objectMapper = new ObjectMapper();
                courtResponse = objectMapper.readValue(
                    response.body(),
                    CourtResponse.class
                );
                LOG.atInfo().log("Response Code: {}, Response Body: {}", response.statusCode(), response.body());
            } else {
                LOG.atError().log("Failed to fetch OU data. HTTP Status: {}", response.statusCode());
            }
        } catch (Exception e) {
            LOG.atError().log("Exception occurred while fetching court room data: {}", e.getMessage());
        }
        return courtResponse;
    }

    private String buildUrl(final String courtId) {
        return UriComponentsBuilder
                .fromUri(URI.create(getCourtHouseClientUrl()))
                .pathSegment(courtId)
                .buildAndExpand(courtId)
                .toUriString();
    }
}
