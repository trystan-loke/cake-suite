package com.cakesuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    
    private boolean isDelivery;
    private BigDecimal deliveryFee;
    private LocalDateTime pickupDate;
    
    private LocalDateTime orderDate;
    private String status;
}
