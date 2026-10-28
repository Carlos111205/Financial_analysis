package com.accounting.revenuerecognition.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Accounting System - Automated Revenue Recognition API")
                        .version("1.0.0")
                        .description("Enterprise RESTful API for financial analysis and automated revenue recognition across Fixed-Price, Time-and-Materials (T&M), and Cost-Plus contracts adhering to ASC 606 / IFRS 15 principles.")
                        .contact(new Contact()
                                .name("Financial Engineering Team")
                                .email("finance-arch@accounting.corp"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
