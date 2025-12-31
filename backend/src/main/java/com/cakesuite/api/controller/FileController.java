package com.cakesuite.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cakesuite.api.dto.SignedUrlDTO;
import com.cakesuite.api.model.User;
import com.cakesuite.api.service.FileService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @GetMapping("signedUrl")
  public ResponseEntity<SignedUrlDTO> getSignedUrl(@AuthenticationPrincipal User user, @RequestParam String fileName, @RequestParam String fileType, @RequestParam double fileSize) {
    return ResponseEntity.ok(fileService.getSignedUrl(user.getId(), fileName, fileType, fileSize));
  }
}
