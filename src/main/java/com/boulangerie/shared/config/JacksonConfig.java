package com.boulangerie.shared.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {

            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(
                    Instant.class,
                    new JsonSerializer<Instant>() {
                        @Override
                        public void serialize(
                                Instant value,
                                JsonGenerator gen,
                                SerializerProvider serializers
                        ) throws IOException {

                            String formatted = DateTimeFormatter
                                    .ofPattern("dd/MM/yyyy HH:mm:ss")
                                    .withZone(ZoneId.of("Africa/Dakar"))
                                    .format(value);

                            gen.writeString(formatted);
                        }
                    }
            );

            builder.modules(module);
        };
    }
}