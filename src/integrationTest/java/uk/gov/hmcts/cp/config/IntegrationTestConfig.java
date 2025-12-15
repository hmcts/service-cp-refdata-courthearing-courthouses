package uk.gov.hmcts.cp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cp.controllers.CourtHousesController;
import uk.gov.hmcts.cp.repositories.InMemoryCourtHousesClientImpl;
import uk.gov.hmcts.cp.services.CourtHousesService;

@Configuration
public class IntegrationTestConfig {
    @Bean("courtHousesService")
    public CourtHousesService courtHousesService() {
        return new CourtHousesService(new InMemoryCourtHousesClientImpl());
    }

    @Bean("courtHousesController")
    public CourtHousesController courtHousesController(final CourtHousesService courtHousesService) {
        return new CourtHousesController(courtHousesService);
    }
}
