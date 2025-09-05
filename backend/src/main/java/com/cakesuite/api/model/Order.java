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
    
    private String orderDetails;
    private BigDecimal totalAmount;
    
    private LocalDateTime orderDate;
    private LocalDateTime dueDate;
    
    private String status;
    
    private String userId;  // Firebase UID of the user who created the order
}
