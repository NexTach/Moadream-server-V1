package com.nextech.moadream.server.v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class MoadreamServerV1Application {

    public static void main(String[] args) {
        SpringApplication.run(MoadreamServerV1Application.class, args);
    }

}
