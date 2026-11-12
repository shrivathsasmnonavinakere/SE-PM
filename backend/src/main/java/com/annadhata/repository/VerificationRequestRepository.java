package com.annadhata.repository;

import com.annadhata.enums.VerificationStatus;
import com.annadhata.model.VerificationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends MongoRepository<VerificationRequest, String> {
    Optional<VerificationRequest> findByUserId(String userId);
    List<VerificationRequest> findByStatus(VerificationStatus status);
    List<VerificationRequest> findAllByOrderBySubmittedAtDesc();
    long countByStatus(VerificationStatus status);
}
