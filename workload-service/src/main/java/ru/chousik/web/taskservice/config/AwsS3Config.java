package ru.chousik.web.taskservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(YandexS3Properties.class)
public class AwsS3Config {

    @Bean
    public S3Client s3Client(YandexS3Properties props) {
        AwsCredentials creds = AwsBasicCredentials.create(
                props.getAccessKey(),
                props.getSecretKey()
        );
        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .httpClient(ApacheHttpClient.create())
                .build();
    }
}
