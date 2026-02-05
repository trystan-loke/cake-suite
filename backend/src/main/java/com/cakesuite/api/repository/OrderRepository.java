package com.cakesuite.api.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.cakesuite.api.model.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);

    @Query("{ 'userId': ?0, 'pickupDate': { $gte: ?1 } }")
    List<Order> findFromDate(String userId, Instant from, Sort sort);

    @Query("{ 'userId': ?0, 'pickupDate': { $lte: ?1 } }")
    List<Order> findToDate(String userId, Instant to, Sort sort);

    @Query("{ 'userId': ?0, 'pickupDate': { $gte: ?1, $lte: ?2 } }")
    List<Order> findByDateRange(String userId, Instant from, Instant to, Sort sort);
}
