package com.annadhata.repository;

import com.annadhata.model.VolunteerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerProfileRepository extends MongoRepository<VolunteerProfile, String> {
    Optional<VolunteerProfile> findByUserId(String userId);
    List<VolunteerProfile> findByAvailableForDispatchTrue();
}

