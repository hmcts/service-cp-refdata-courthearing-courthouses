package uk.gov.hmcts.cp.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    private OpenAPIConfigurationLoader openAPIConfigLoader = new OpenAPIConfigurationLoader();

    @Bean
    public OpenAPI openAPI() {
        return openAPIConfigLoader.openAPI();
    }
}
