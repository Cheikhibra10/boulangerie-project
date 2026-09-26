package com.boulangerie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Modulith
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableScheduling
public class BoulangerieApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoulangerieApplication.class, args);
    }
}
