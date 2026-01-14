package uk.gov.hmcts.cp.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;

import java.net.URI;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourtHousesClient {

    private final AppPropertiesBackend appProperties;
    private final RestTemplate restTemplate;

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

    private CourtResponse getCourtHouseAndRoomDetails(final String courtId) {
        final String url = buildUrl(courtId);
        log.info("Getting courtrooms from url:{}", url);
        final HttpEntity<String> requestEntity = getRequestEntity();
        final ResponseEntity<CourtResponse> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            CourtResponse.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            log.error("getCourtHouseAndRoomDetails response:{}", response.getStatusCode());
            throw new HttpServerErrorException(response.getStatusCode());
        }
    }

    private String buildUrl(final String courtId) {
        return UriComponentsBuilder
            .fromUri(URI.create(appProperties.getBackendUrl()))
            .path(appProperties.getBackendPath())
            .pathSegment(courtId)
            .buildAndExpand(courtId)
            .toUriString();
    }

    public HttpEntity<String> getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.referencedata.ou-courtroom+json");
        headers.add("CJSCPPUID", appProperties.getBackendCjscppuid());
        return new HttpEntity<>(headers);
    }
}
