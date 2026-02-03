package uk.gov.hmcts.cp.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cp.config.AppPropertiesBackend;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.mappers.CourtHouseMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourtHousesClient {

    private final AppPropertiesBackend appProperties;
    private final RestTemplate restTemplate;
    private final CourtHouseMapper mapper;

    public CourtResponse getCourtHouse(final UUID courtId) {
        return getCourtHouseDetails(courtId);
    }

    private CourtResponse getCourtHouseDetails(final UUID courtId) {
        final String url = buildUrl(courtId);
        log.info("Getting courtrooms from url:{}", url);
        final HttpEntity<String> requestEntity = getRequestEntity();
        final ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            String.class
        );
        final String emptyBody = "{}";
        if (response.getBody().equals(emptyBody)) {
            log.error("getCourtHouseDetails returned empty response");
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else if (response.getStatusCode().is2xxSuccessful()) {
            return mapper.mapStringToCourtResponse(response.getBody());
        } else {
            log.error("getCourtHouseDetails response:{}", response.getStatusCode());
            throw new HttpServerErrorException(response.getStatusCode());
        }
    }

    private String buildUrl(final UUID courtId) {
        return String.format("%s%s/%s", appProperties.getBackendUrl(), appProperties.getBackendPath(), courtId);
    }

    public HttpEntity<String> getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.referencedata.ou-courtroom+json");
        headers.add("CJSCPPUID", appProperties.getBackendCjscppuid());
        return new HttpEntity<>(headers);
    }
}
