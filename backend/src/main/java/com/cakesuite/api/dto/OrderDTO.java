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
    private String orderDetails;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime dueDate;
    private String status;
}
