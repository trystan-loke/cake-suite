package com.cakesuite.api.service;

import com.cakesuite.api.dto.OrderDTO;
import com.cakesuite.api.model.Order;
import com.cakesuite.api.model.User;
import com.cakesuite.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public List<OrderDTO> getAllOrders(User user) {
        List<Order> orders = orderRepository.findByUserId(user.getId());
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public OrderDTO getOrderById(String id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Ensure the order belongs to the requesting user
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToDTO(order);
    }
    
    public OrderDTO createOrder(OrderDTO orderDTO, User user) {
        Order order = convertToEntity(orderDTO);
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    public OrderDTO updateOrder(String id, OrderDTO orderDTO, User user) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Ensure the order belongs to the requesting user
        if (!existingOrder.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        // Update fields
        existingOrder.setCustomerName(orderDTO.getCustomerName());
        existingOrder.setCustomerPhone(orderDTO.getCustomerPhone());
        existingOrder.setCustomerEmail(orderDTO.getCustomerEmail());
        existingOrder.setOrderDetails(orderDTO.getOrderDetails());
        existingOrder.setTotalAmount(orderDTO.getTotalAmount());
        existingOrder.setDueDate(orderDTO.getDueDate());
        existingOrder.setStatus(orderDTO.getStatus());
        
        Order updatedOrder = orderRepository.save(existingOrder);
        return convertToDTO(updatedOrder);
    }
    
    public void deleteOrder(String id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Ensure the order belongs to the requesting user
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        orderRepository.delete(order);
    }
    
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .orderDetails(order.getOrderDetails())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .dueDate(order.getDueDate())
                .status(order.getStatus())
                .build();
    }
    
    private Order convertToEntity(OrderDTO orderDTO) {
        return Order.builder()
                .id(orderDTO.getId())
                .customerName(orderDTO.getCustomerName())
                .customerPhone(orderDTO.getCustomerPhone())
                .customerEmail(orderDTO.getCustomerEmail())
                .orderDetails(orderDTO.getOrderDetails())
                .totalAmount(orderDTO.getTotalAmount())
                .dueDate(orderDTO.getDueDate())
                .status(orderDTO.getStatus())
                .build();
    }
}
