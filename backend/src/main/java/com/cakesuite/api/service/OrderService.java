package com.cakesuite.api.service;

import com.cakesuite.api.dto.OrderDTO;
import com.cakesuite.api.model.Order;
import com.cakesuite.api.model.User;
import com.cakesuite.api.repository.OrderRepository;
import com.cakesuite.api.util.IdGenerator;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;

    private final FileService fileService;

    @Value("${gcp.storage.upload-path-temp}")
    private String tempPath;
    
    public List<OrderDTO> getAllOrders(User user, Instant from, Instant to) {
        // If neither from nor to is provided, default from to today
        if(from == null && to == null) {
            from = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        
        List<Order> orders;
        if(from != null && to != null) {
            orders = orderRepository.findByDateRange(user.getId(), from, to, Sort.by(Sort.Direction.ASC, "pickupDate"));
        } else if(from != null) {
            orders = orderRepository.findFromDate(user.getId(), from, Sort.by(Sort.Direction.ASC, "pickupDate"));
        } else {
            orders = orderRepository.findToDate(user.getId(), to, Sort.by(Sort.Direction.ASC, "pickupDate"));
        }
        
        List<OrderDTO> result = orders.stream()
                .map(o -> {
                    OrderDTO dto = convertToDTO(o);
                    dto.setImages(dto.getImages().stream()
                        .map(image -> OrderDTO.ImageDTO.builder()
                                .url(fileService.getReadSignedUrl(image.getPath()))
                                .path(image.getPath())
                                .build())
                        .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
        return result;
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
        List<OrderDTO.ImageDTO> newImage = new ArrayList<>();
        orderDTO.getImages().forEach(image -> {
            // Extract filename from temp path
            String filename = image.getPath().substring(image.getPath().lastIndexOf('/') + 1);

            // Construct new permanent path with user ID folder
            String permanentPath = "file/" + user.getId() + "/" + newOrderNo + "/" + filename.substring(filename.indexOf('_') + 1);

            fileService.moveFile(image.getPath(), permanentPath);
            newImage.add(OrderDTO.ImageDTO.builder()
                .url(null)
                .path(permanentPath)
                .build());
        });
        orderDTO.setImages(newImage);
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

        // Handle image updates
        List<OrderDTO.ImageDTO> newImages = new ArrayList<>();
        List<String> existingImageUrls = existingOrder.getImages() != null
                ? existingOrder.getImages().stream()
                        .map(Order.OrderImage::getImageUrl)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        if (orderDTO.getImages() != null) {
            for (OrderDTO.ImageDTO image : orderDTO.getImages()) {
                if (image.getPath().startsWith(tempPath)) {
                    String filename = image.getPath().substring(image.getPath().lastIndexOf('/') + 1);

                    String permanentPath = "file/" + user.getId() + "/" + existingOrder.getOrderNo() + "/"
                            + filename.substring(filename.indexOf('_') + 1);

                    fileService.moveFile(image.getPath(), permanentPath);
                    newImages.add(OrderDTO.ImageDTO.builder()
                        .url(null)
                        .path(permanentPath)
                        .build());
                } else {
                    newImages.add(image);
                }
            }

            // Delete images that were removed (exist in old order but not in new)
            for (String oldImageUrl : existingImageUrls) {
                if (!newImages.stream().map(OrderDTO.ImageDTO::getPath).collect(Collectors.toList()).contains(oldImageUrl)) {
                    fileService.deleteFile(oldImageUrl, user.getId(), existingOrder.getOrderNo());
                }
            }
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
        existingOrder.setOrderDate(orderDTO.getOrderDate());
        existingOrder.setStatus(orderDTO.getStatus());
        // Update images
        existingOrder.setImages(newImages.stream()
                .map(image -> Order.OrderImage.builder()
                        .imageUrl(image.getPath())
                        .uploadedAt(Instant.now())
                        .build())
                .collect(Collectors.toList()));
        
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

    public void exportOrdersToCsv(User user, Instant from, Instant to, HttpServletResponse response) throws IOException {
    
    List<OrderDTO> orders = getAllOrders(user, from, to);

    // Set response headers for CSV download
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"orders_" + 
        LocalDate.now().toString() + ".csv\"");

    // Create CSV writer
    try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
            .withHeader("Pickup Date", "Customer Name", "Order Summary", "Order Details", 
                       "Total Amount", "Deposit", "Delivery", "Status"))) {
        
        for (OrderDTO order : orders) {
            csvPrinter.printRecord(
                order.getPickupDate() != null ? 
                    LocalDate.ofInstant(order.getPickupDate(), ZoneId.systemDefault()).toString() : "",
                order.getCustomerName(),
                order.getOrderSummary(),
                order.getOrderDetails() != null ? order.getOrderDetails() : "",
                order.getTotalAmount(),
                order.getDeposit() != null ? order.getDeposit() : 0,
                order.isDelivery() ? "Yes" : "No",
                order.getStatus()
            );
        }
        
        csvPrinter.flush();
    }
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
                .images(order.getImages() != null ? order.getImages().stream()
                        .map(image -> OrderDTO.ImageDTO.builder()
                                .url(null)
                                .path(image.getImageUrl())
                                .build())
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
                .images(orderDTO.getImages() != null ? orderDTO.getImages().stream()
                    .map(image -> Order.OrderImage.builder()
                            .imageUrl(image.getPath())
                            .uploadedAt(Instant.now())
                            .build())
                    .collect(Collectors.toList())
                    : new ArrayList<>())
                .build();
    }
}
