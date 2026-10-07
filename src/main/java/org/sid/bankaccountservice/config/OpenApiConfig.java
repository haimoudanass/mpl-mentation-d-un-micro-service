package org.sid.bankaccountservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankAccountOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Account Microservice")
                        .description("API REST de gestion des comptes bancaires (démarche Youssfi)")
                        .version("1.0"));
    }
}
