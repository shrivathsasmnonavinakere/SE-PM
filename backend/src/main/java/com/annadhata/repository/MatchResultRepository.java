package com.annadhata.repository;

import com.annadhata.model.MatchResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends MongoRepository<MatchResult, String> {
    List<MatchResult> findByDonationIdOrderByMatchScoreDesc(String donationId);
    List<MatchResult> findByReceiverIdOrderByMatchScoreDesc(String receiverId);
    void deleteByDonationId(String donationId);
}
