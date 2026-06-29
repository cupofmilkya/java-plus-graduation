package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.web",
        "ru.practicum.category",
        "ru.practicum.feign",
        "ru.practicum.exception",
        "ru.practicum.dto"
})
@EnableDiscoveryClient
public class MainServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);
    }
}
