package com.cakesuite.api.service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cakesuite.api.dto.SignedUrlDTO;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;

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
  public SignedUrlDTO getSignedUrl(String userId, String fileName, String fileType, double fileSize) {
    // Santitize file name
    fileName = fileName.replaceAll("[^a-zA-Z0-9.-]+", "_");

    // Validate file size
    if(fileSize > maxFileSize) {
      throw new RuntimeException("File size exceeds limit");
    }

    String filePath = tempPath + userId + "_" + System.currentTimeMillis() + "_" + fileName;
    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath).setContentType(fileType).build();

    URL signedUrl = storage.signUrl(
      blobInfo,
      signedUrlExpiry,
      TimeUnit.MINUTES,
      Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
      Storage.SignUrlOption.withV4Signature()
    );
    
    return SignedUrlDTO.builder().signedUrl(signedUrl.toString()).tempPath(filePath).build();
  }
}
