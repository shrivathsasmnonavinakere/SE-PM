package com.annadhata.repository;

import com.annadhata.enums.PassportStatus;
import com.annadhata.model.FoodPassport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodPassportRepository extends MongoRepository<FoodPassport, String> {
    Optional<FoodPassport> findByClaimId(String claimId);
    Optional<FoodPassport> findByPassportCode(String passportCode);
    List<FoodPassport> findByDonationId(String donationId);
    List<FoodPassport> findByReceiverIdAndStatusOrderByCreatedAtDesc(String receiverId, PassportStatus status);
    List<FoodPassport> findByDonorIdOrderByCreatedAtDesc(String donorId);
}

