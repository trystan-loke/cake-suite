package com.cakesuite.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String orderNo;

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
    private Instant pickupDate;
    
    private Instant orderDate;
    
    @Builder.Default
    private String status = "Confirmed";
    
    private String userId;  // Firebase UID of the user who created the order
    
    private List<OrderImage> images;

    // Images
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderImage {
        @Builder.Default
        private String id = UUID.randomUUID().toString();;
        private String imageUrl;
        private Instant uploadedAt;
    }
}
