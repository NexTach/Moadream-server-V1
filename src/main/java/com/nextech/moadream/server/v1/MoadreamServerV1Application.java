package com.nextech.moadream.server.v1;

import com.nextach.megamethod.MegaMethod;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.nextech.moadream.server.v1.domain")
public class MoadreamServerV1Application {

    public static void main(String[] args) {
        System.out.println(MegaMethod.generateRandomSentence(20));
        SpringApplication.run(MoadreamServerV1Application.class, args);
    }

}
