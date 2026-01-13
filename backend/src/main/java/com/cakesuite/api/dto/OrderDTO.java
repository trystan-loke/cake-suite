package com.cakesuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    private String orderSummary;
    private String orderDetails;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private BigDecimal tip;
    
    @JsonProperty("isDelivery")
    private boolean isDelivery;
    private BigDecimal deliveryFee;
    private LocalDateTime pickupDate;
    
    private LocalDateTime orderDate;
    private String status;
    private List<String> imagePaths;
}
