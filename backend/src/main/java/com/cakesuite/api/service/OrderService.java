package com.cakesuite.api.service;

import com.cakesuite.api.dto.OrderDTO;
import com.cakesuite.api.model.Order;
import com.cakesuite.api.model.User;
import com.cakesuite.api.repository.OrderRepository;
import com.cakesuite.api.util.IdGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;

    private final FileService fileService;
    
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
        String newOrderNo = IdGenerator.generateOrderNo();
        // Move temp image path to permanent path
        orderDTO.getImagePaths().forEach(imagePath -> {
            // Extract filename from temp path
            String filename = imagePath.substring(imagePath.lastIndexOf('/') + 1);

            // Construct new permanent path with user ID folder
            String permanentPath = "file/" + user.getId() + "/" + newOrderNo + "/" + filename.substring(filename.indexOf('_') + 1);

            fileService.moveFile(imagePath, permanentPath);
            imagePath = permanentPath; // Update the image URL
        });
        Order order = convertToEntity(orderDTO);
        order.setOrderNo(newOrderNo);
        order.setUserId(user.getId());
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
        existingOrder.setOrderSummary(orderDTO.getOrderSummary());
        existingOrder.setOrderDetails(orderDTO.getOrderDetails());
        existingOrder.setTotalAmount(orderDTO.getTotalAmount());
        existingOrder.setDeposit(orderDTO.getDeposit());
        existingOrder.setTip(orderDTO.getTip());
        existingOrder.setDelivery(orderDTO.isDelivery());
        existingOrder.setDeliveryFee(orderDTO.getDeliveryFee());
        existingOrder.setPickupDate(orderDTO.getPickupDate());
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
                .orderSummary(order.getOrderSummary())
                .orderDetails(order.getOrderDetails())
                .totalAmount(order.getTotalAmount())
                .deposit(order.getDeposit())
                .tip(order.getTip())
                .isDelivery(order.isDelivery())
                .deliveryFee(order.getDeliveryFee())
                .pickupDate(order.getPickupDate())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .imagePaths(order.getImages() != null ? order.getImages().stream()
                        .map(Order.OrderImage::getImageUrl)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }
    
    private Order convertToEntity(OrderDTO orderDTO) {
        return Order.builder()
                .id(orderDTO.getId())
                .customerName(orderDTO.getCustomerName())
                .customerPhone(orderDTO.getCustomerPhone())
                .customerEmail(orderDTO.getCustomerEmail())
                .orderSummary(orderDTO.getOrderSummary())
                .orderDetails(orderDTO.getOrderDetails())
                .totalAmount(orderDTO.getTotalAmount())
                .deposit(orderDTO.getDeposit())
                .tip(orderDTO.getTip())
                .isDelivery(orderDTO.isDelivery())
                .deliveryFee(orderDTO.getDeliveryFee())
                .orderDate(orderDTO.getOrderDate())
                .pickupDate(orderDTO.getPickupDate())
                .status(orderDTO.getStatus())
                .images(orderDTO.getImagePaths() != null ? orderDTO.getImagePaths().stream()
                    .map(imagePath -> Order.OrderImage.builder()
                            .imageUrl(imagePath)
                            .uploadedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList())
                    : new ArrayList<>())
                .build();
    }
}
