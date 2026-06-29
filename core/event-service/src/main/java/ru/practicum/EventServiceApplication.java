package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.web",
        "ru.practicum.feign",
        "ru.practicum.exception",
        "ru.practicum.dto",
        "ru.practicum.validation"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.practicum.feign")
@EntityScan(basePackages = {
        "ru.practicum.category.entity",
        "ru.practicum.web.category.entity",
        "ru.practicum.web.event.entity",
        "ru.practicum.web.admin.entity",
        "ru.practicum.web.user.entity"
})
@EnableJpaRepositories(basePackages = {
        "ru.practicum.web.category.repository",
        "ru.practicum.web.event.repository",
        "ru.practicum.web.admin.repository"
})
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}