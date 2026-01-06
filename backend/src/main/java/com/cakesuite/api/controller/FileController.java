package com.cakesuite.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cakesuite.api.dto.SignedUrlReqDTO;
import com.cakesuite.api.dto.SignedUrlResDTO;
import com.cakesuite.api.model.User;
import com.cakesuite.api.service.FileService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @PostMapping("signedUrl")
  public ResponseEntity<List<SignedUrlResDTO>> getSignedUrl(@AuthenticationPrincipal User user, @RequestBody List<SignedUrlReqDTO> req) {
    return ResponseEntity.ok(fileService.getSignedUrl(user.getId(), req));
  }
}
