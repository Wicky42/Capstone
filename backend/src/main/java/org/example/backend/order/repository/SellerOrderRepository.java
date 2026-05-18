package org.example.backend.order.repository;

import org.example.backend.order.model.SellerOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerOrderRepository extends MongoRepository<SellerOrder, String> {
    List<SellerOrder> getSellerOrderBySellerId(String id);
    List<SellerOrder> findByFulfillmentOrderId(String fulfillmentOrderId);
}
