package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.statsclient.config.GrpcClientConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(GrpcClientConfig.class)
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}