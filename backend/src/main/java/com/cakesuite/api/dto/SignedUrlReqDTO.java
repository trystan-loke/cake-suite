package com.cakesuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlReqDTO {
  private String fileName;
  private String fileType;
  private double fileSize;
}
