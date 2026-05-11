package com.dhruv.claimsrouter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI claimsRouterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Claims Router API")
                        .description("REST API for healthcare claim ingestion, validation, and routing.")
                        .version("v1")
                        .contact(new Contact().name("Dhruv Bhanderi"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
