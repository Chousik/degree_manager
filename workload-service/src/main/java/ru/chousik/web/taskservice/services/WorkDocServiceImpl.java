package ru.chousik.web.taskservice.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.chousik.web.taskservice.blob.FileResource;
import ru.chousik.web.taskservice.config.YandexS3Properties;
import ru.chousik.web.taskservice.repository.WorkRepository;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkDocServiceImpl implements WorkDocService {
    S3Client s3Client;
    String bucket;
    WorkRepository workRepository;
    public WorkDocServiceImpl(S3Client s3Client,
                           YandexS3Properties props,
                              WorkRepository workRepository){
        this.s3Client = s3Client;
        this.bucket = props.getBucket();
        this.workRepository = workRepository;
    }

    @Override
    public void uploadWork(String key,
                           FileResource fileResource){
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(fileResource.contentType())
                .build();

        s3Client.putObject(putReq, RequestBody.fromInputStream(fileResource.stream(),
                fileResource.length()));

    }

    @Override
    public ResponseEntity<InputStreamResource> downloadWork(UUID uuid){

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(workRepository.findKeyByUuid(uuid))
                .build();
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getReq);
        InputStreamResource resource = new InputStreamResource(s3Object);
        String contentType = s3Object.response().contentType();
        long contentLength = s3Object.response().contentLength();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .contentLength(contentLength)
                .body(resource);
    }

    @Override
    public void deleteWorkFile(String key) {
        DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(delReq);
    }
}
