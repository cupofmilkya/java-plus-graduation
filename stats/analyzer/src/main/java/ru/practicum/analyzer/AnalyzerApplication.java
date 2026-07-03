package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.grpc.RecommendationsGrpcController;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "ru.practicum.analyzer",
        "ru.practicum.grpc",
        "ru.practicum.service",
        "ru.practicum.repository",
        "ru.practicum.model"
})
@EnableJpaRepositories(basePackages = "ru.practicum.repository")
@EntityScan(basePackages = "ru.practicum.model")
public class AnalyzerApplication {

    @Bean
    public String checkGrpcController(RecommendationsGrpcController controller) {
        System.out.println("=== RecommendationsGrpcController BEAN CREATED: " + controller + " ===");
        return "ok";
    }

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}