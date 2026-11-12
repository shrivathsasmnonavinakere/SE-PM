package com.annadhata.repository;

import com.annadhata.enums.FoodCategory;
import com.annadhata.model.ImpactRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImpactRecordRepository extends MongoRepository<ImpactRecord, String> {
    List<ImpactRecord> findByDonorId(String donorId);
    List<ImpactRecord> findByReceiverId(String receiverId);
    List<ImpactRecord> findByFoodCategory(FoodCategory foodCategory);
    List<ImpactRecord> findAllByOrderByCreatedAtDesc();
    boolean existsByClaimId(String claimId);
    java.util.Optional<ImpactRecord> findByClaimId(String claimId);
}
