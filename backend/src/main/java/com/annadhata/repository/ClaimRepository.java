package com.annadhata.repository;

import com.annadhata.enums.ClaimStatus;
import com.annadhata.model.Claim;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends MongoRepository<Claim, String> {
    List<Claim> findByDonationId(String donationId);
    List<Claim> findByReceiverIdOrderByCreatedAtDesc(String receiverId);
    List<Claim> findByDonorIdOrderByCreatedAtDesc(String donorId);
    List<Claim> findByAssignedVolunteerIdOrderByCreatedAtDesc(String volunteerId);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByStatusAndConfirmedPickupTimeBefore(ClaimStatus status, Instant threshold);
    Optional<Claim> findByDonationIdAndReceiverId(String donationId, String receiverId);
    Optional<Claim> findByDonationIdAndStatus(String donationId, ClaimStatus status);
    boolean existsByDonationIdAndStatus(String donationId, ClaimStatus status);
    long countByStatus(ClaimStatus status);
    long countByReceiverId(String receiverId);
    long countByReceiverIdAndStatus(String receiverId, ClaimStatus status);
    long countByDonorId(String donorId);
    long countByAssignedVolunteerId(String volunteerId);
    long countByAssignedVolunteerIdAndStatus(String volunteerId, ClaimStatus status);
}
