package ru.chousik.web.taskservice.services;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.chousik.web.taskservice.config.YandexS3Properties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URL;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkDocServiceImpl implements WorkDocService {
    S3Client s3Client;
    String bucket;
    public WorkDocServiceImpl(S3Client s3Client,
                           YandexS3Properties props){
        this.s3Client = s3Client;
        this.bucket = props.getBucket();
    }
    @Override
    public URL uploadWork(String key,
                          InputStream data,
                          long length,
                          String contType){
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contType)
                .build();

        s3Client.putObject(putReq, RequestBody.fromInputStream(data, length));

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build());


    }
}
