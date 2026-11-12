package com.annadhata.repository;

import com.annadhata.enums.FoodCategory;
import com.annadhata.model.ReceiverNeed;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiverNeedRepository extends MongoRepository<ReceiverNeed, String> {
    List<ReceiverNeed> findByReceiverId(String receiverId);
    List<ReceiverNeed> findByReceiverIdAndActiveTrue(String receiverId);
    List<ReceiverNeed> findByActiveTrue();
    List<ReceiverNeed> findByRequiredCategoryAndActiveTrue(FoodCategory category);
    long countByActiveTrue();
}
