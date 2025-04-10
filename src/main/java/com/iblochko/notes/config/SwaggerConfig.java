package com.iblochko.notes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notes Application API")
                        .description("RESTful API for managing personal notes")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Daniil Tarashkevich")
                                .email("danilatarashkevich@gmail.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}
