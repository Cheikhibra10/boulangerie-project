package com.boulangerie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.Modulith;

@SpringBootApplication
@Modulith
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class BoulangerieApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoulangerieApplication.class, args);
    }
}