package com.annadhata.repository;

import com.annadhata.enums.RescueRelayStatus;
import com.annadhata.model.RescueRelayEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RescueRelayEventRepository extends MongoRepository<RescueRelayEvent, String> {
    Optional<RescueRelayEvent> findByClaimId(String claimId);
    List<RescueRelayEvent> findByDonorIdOrderByDetectedAtDesc(String donorId);
    List<RescueRelayEvent> findByOriginalReceiverIdOrderByDetectedAtDesc(String receiverId);
    List<RescueRelayEvent> findByStatusOrderByDetectedAtDesc(RescueRelayStatus status);
    List<RescueRelayEvent> findByDonorIdAndStatusOrderByDetectedAtDesc(String donorId, RescueRelayStatus status);
    List<RescueRelayEvent> findByDonationId(String donationId);
    boolean existsByClaimId(String claimId);
}

