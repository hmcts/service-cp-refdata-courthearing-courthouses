package uk.gov.hmcts.cp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class AppPropertiesBackend {

    private final String backendUrl;
    private final String backendPath;
    private final String backendCjscppuid;

    public AppPropertiesBackend(
        @Value("${service.court-house-client.url}") final String backendUrl,
        @Value("${service.court-house-client.path}") final String backendPath,
        @Value("${service.court-house-client.cjscppuid}") final String backendCjscppuid) {
        this.backendUrl = backendUrl;
        this.backendPath = backendPath;
        this.backendCjscppuid = backendCjscppuid;
    }
}
