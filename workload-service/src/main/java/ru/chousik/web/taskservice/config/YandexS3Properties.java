package ru.chousik.web.taskservice.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConfigurationProperties(prefix = "yandex.s3")
public class YandexS3Properties {
    String endpoint;
    String region;
    String bucket;
    String accessKey;
    String secretKey;
}
