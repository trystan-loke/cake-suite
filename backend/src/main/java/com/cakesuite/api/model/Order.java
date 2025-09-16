package com.cakesuite.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
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
    
    @Builder.Default
    private String status = "Confirmed";
    
    private String userId;  // Firebase UID of the user who created the order
}
