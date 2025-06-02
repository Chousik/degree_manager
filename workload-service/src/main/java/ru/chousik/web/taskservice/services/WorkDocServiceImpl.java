package ru.chousik.web.taskservice.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.chousik.web.taskservice.config.YandexS3Properties;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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
    public void uploadWork(String key,
                          InputStream data,
                          long length,
                          String contType){
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contType)
                .build();

        s3Client.putObject(putReq, RequestBody.fromInputStream(data, length));

    }
    @Override
    public ResponseEntity<InputStreamResource> downloadWork(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getReq);
        InputStreamResource resource = new InputStreamResource(s3Object);
        String contentType = s3Object.response().contentType();
        long contentLength = s3Object.response().contentLength();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .contentLength(contentLength)
                .body(resource);
    }
}
