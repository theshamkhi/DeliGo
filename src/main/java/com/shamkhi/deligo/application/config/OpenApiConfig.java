package com.shamkhi.deligo.application.config;

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
                        .title("DeliGo")
                        .version("1.0")
                        .description("API REST pour un Système de gestion de livraison intelligent")
                        .contact(new Contact()
                                .name("DeliGo")
                                .email("contact@deligo.ma")));
    }
}