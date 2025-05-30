package ru.chousik.web.authservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YandexS3Properties.class)
public class AwsS3Config {

    @Bean
    public S3Client s3Client(YandexS3Properties props) {
        AwsCredentials creds = AwsBasicCredentials.create(
                props.getCredentials().getAccessKey(),
                props.getCredentials().getSecretKey());

        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .httpClient(ApacheHttpClient.create())
                .build();
    }
}
