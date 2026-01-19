package com.cakesuite.api.controller;

import com.cakesuite.api.dto.OrderDTO;
import com.cakesuite.api.model.User;
import com.cakesuite.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@RequestParam(value = "from", required = false) Instant from) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(orderService.getAllOrders(currentUser, from));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String id) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(orderService.getOrderById(id, currentUser));
    }
    
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(orderService.createOrder(orderDTO, currentUser));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable String id, @RequestBody OrderDTO orderDTO) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(orderService.updateOrder(id, orderDTO, currentUser));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        User currentUser = getCurrentUser();
        orderService.deleteOrder(id, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }
}
