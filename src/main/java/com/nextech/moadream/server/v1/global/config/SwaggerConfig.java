package com.nextech.moadream.server.v1.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setDescription("개발 서버");

        Contact contact = new Contact();
        contact.setName("세모이");

        License license = new License().name("Copyright © 2025 세모이")
                .url("https://github.com/NexTach/Moadream-server-V1");

        Info info = new Info().title("모아드림 (Moadream) API").version("1.0.0")
                .description("실시간 전기·가스·수도 사용량 및 요금 확인 서비스 API").contact(contact).license(license);

        SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                .bearerFormat("JWT").in(SecurityScheme.In.HEADER).name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI().info(info).servers(List.of(devServer))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
