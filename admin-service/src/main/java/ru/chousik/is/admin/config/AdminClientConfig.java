package ru.chousik.is.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(UserServiceProperties.class)
public class AdminClientConfig {

    @Bean
    public WebClient userServiceWebClient(UserServiceProperties properties, WebClient.Builder builder) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
