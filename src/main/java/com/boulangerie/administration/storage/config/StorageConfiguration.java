package com.boulangerie.administration.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class StorageConfiguration {

    @Bean
    RestClient cloudinaryRestClient(RestClient.Builder builder) {
        return builder.build();
    }

}