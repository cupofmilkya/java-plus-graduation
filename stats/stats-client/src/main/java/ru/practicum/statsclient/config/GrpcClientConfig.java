package ru.practicum.statsclient.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.service.collector.UserActionControllerGrpc;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc;

@Configuration
public class GrpcClientConfig {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    @Bean
    public UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub() {
        return collectorStub;
    }

    @Bean
    public RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub() {
        return analyzerStub;
    }
}