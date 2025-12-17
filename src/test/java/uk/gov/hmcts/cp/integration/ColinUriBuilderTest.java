package uk.gov.hmcts.cp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ColinUriBuilderTest {

    @Test
    void uri_builder_should_do_as_expected(){
        String url = buildUrl("1234");
        assertThat(url).isEqualTo("https://CAOURT_HOUSE.org.uk/referencedata-service/query/api/rest/referencedata/courtrooms/1234");
    }

    private String getCourtHouseClientUrl(){
        return "https://CAOURT_HOUSE.org.uk";
    }

    private String getCourtHouseClientPath(){
        return "/referencedata-service/query/api/rest/referencedata/courtrooms";
    }

    private String buildUrl(final String courtId) {
        return UriComponentsBuilder
            .fromUri(URI.create(getCourtHouseClientUrl()))
            .path(getCourtHouseClientPath())
            .pathSegment(courtId)
            .buildAndExpand(courtId)
            .toUriString();
    }
}
