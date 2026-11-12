package com.annadhata.repository;

import com.annadhata.enums.DonationStatus;
import com.annadhata.enums.FoodCategory;
import com.annadhata.model.Donation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DonationRepository extends MongoRepository<Donation, String> {
    List<Donation> findByDonorId(String donorId);
    List<Donation> findByDonorIdOrderByCreatedAtDesc(String donorId);
    List<Donation> findByStatus(DonationStatus status);
    List<Donation> findByStatusOrderByCreatedAtDesc(DonationStatus status);
    List<Donation> findByStatusAndPickupDeadlineAfterOrderByPickupDeadlineAsc(DonationStatus status, Instant now);
    List<Donation> findByStatusAndFoodCategoryAndPickupDeadlineAfterOrderByPickupDeadlineAsc(DonationStatus status, FoodCategory category, Instant now);
    
    @Query("{'status': {'$in': ['AVAILABLE', 'MATCHED', 'PARTIALLY_CLAIMED']}, 'pickupDeadline': {'$lt': ?0}}")
    List<Donation> findExpiredAvailableDonations(Instant now);

    long countByStatus(DonationStatus status);
    long countByDonorId(String donorId);
    long countByDonorIdAndStatus(String donorId, DonationStatus status);
}
