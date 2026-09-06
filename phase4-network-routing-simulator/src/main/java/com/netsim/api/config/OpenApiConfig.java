package com.netsim.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI is served at /swagger-ui.html; raw spec at /v3/api-docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI networkSimulatorOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Network Routing Simulator API")
                .description("REST + WebSocket API for the dynamic network routing and packet simulation system")
                .version("2.0.0"));
    }
}
