package com.nextech.moadream.server.v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class MoadreamServerV1Application {

    public static void main(String[] args) {
        SpringApplication.run(MoadreamServerV1Application.class, args);
    }

}
