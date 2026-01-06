package com.cakesuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlResDTO {
  private String signedUrl;
  private String tempPath;
  private String fileName;
}
