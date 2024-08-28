package com.example.gyeongjoLog.event.config;

import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import com.example.gyeongjoLog.event.repository.EventTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(EventTypeRepository eventTypeRepository) {
        return args -> {
            // Check if initial data already exists
            if (eventTypeRepository.count() == 0) {
                eventTypeRepository.save(EventTypeEntity.builder().eventType("결혼식").color("PinkCustom").build());
                eventTypeRepository.save(EventTypeEntity.builder().eventType("장례식").color("BlackCustom").build());
                eventTypeRepository.save(EventTypeEntity.builder().eventType("생일").color("OrangeCustom").build());
                eventTypeRepository.save(EventTypeEntity.builder().eventType("돌잔치").color("Blue-Selection").build());
            }
        };
    }
}
