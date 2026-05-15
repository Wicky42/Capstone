package org.example.backend.order.repository;

import org.example.backend.order.model.FulfillmentOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FulfillmentOrderRepository extends MongoRepository<FulfillmentOrder, String> {
}
