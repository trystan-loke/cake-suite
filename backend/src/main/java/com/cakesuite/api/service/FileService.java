package com.cakesuite.api.service;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cakesuite.api.dto.SignedUrlReqDTO;
import com.cakesuite.api.dto.SignedUrlResDTO;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.CopyRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

  @Value("${gcp.storage.bucket-name}")
  private String bucketName;

  @Value("${gcp.storage.upload-path-temp}")
  private String tempPath;

  @Value("${gcp.storage.upload-path}")
  private String uploadPath;

  @Value("${gcp.storage.signed-url-expiry}")
  private int signedUrlExpiry;

  @Value("${gcp.storage.signed-url-read-expiry}")
  private int signedUrlReadExpiry;

  @Value("${gcp.storage.max-file-size}")
  private int maxFileSize;

  private final Storage storage;

  /***
   * @param userId
   * @param fileName
   * @param fileType
   * @param fizeSize in Mb
   * @return
   */
  public List<SignedUrlResDTO> getSignedUrl(String userId, List<SignedUrlReqDTO> req) {
    return req.stream().map(file -> {
      String sanitizedName = file.getFileName().replaceAll("[^a-zA-Z0-9.-]+", "_");
      
      if(file.getFileSize() > maxFileSize) {
        throw new RuntimeException("File size exceeds limit: " + file.getFileName());
      }

      String filePath = tempPath + userId + "_" + System.currentTimeMillis() + "_" + sanitizedName;
      BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath)
          .setContentType(file.getFileType())
          .build();

      URL signedUrl = storage.signUrl(
        blobInfo,
        signedUrlExpiry,
        TimeUnit.MINUTES,
        Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
        Storage.SignUrlOption.withV4Signature()
      );
      
      return SignedUrlResDTO.builder()
          .signedUrl(signedUrl.toString())
          .tempPath(filePath)
          .fileName(file.getFileName())
          .build();
    }).collect(Collectors.toList());
  }

  public void moveFile(String sourcePath, String destinationPath) {
    BlobId source = BlobId.of(bucketName, sourcePath);
    BlobId target = BlobId.of(bucketName, destinationPath);

    CopyRequest request = CopyRequest.newBuilder()
        .setSource(source)
        .setTarget(target)
        .build();

    storage.copy(request).getResult();
    storage.delete(source);
  }

  public String getReadSignedUrl(String filePath) {
    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath).build();

    URL signedUrl = storage.signUrl(
        blobInfo,
        signedUrlReadExpiry,
        TimeUnit.MINUTES,
        Storage.SignUrlOption.httpMethod(HttpMethod.GET),
        Storage.SignUrlOption.withV4Signature());

    return signedUrl.toString();
  }
}
