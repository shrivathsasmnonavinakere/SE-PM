package com.annadhata.repository;

import com.annadhata.model.TrustScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrustScoreRepository extends MongoRepository<TrustScore, String> {
    Optional<TrustScore> findByUserId(String userId);
}

