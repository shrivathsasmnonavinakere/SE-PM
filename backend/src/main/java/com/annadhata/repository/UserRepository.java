package com.annadhata.repository;

import com.annadhata.enums.Role;
import com.annadhata.enums.VerificationStatus;
import com.annadhata.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndActiveTrue(Role role);
    List<User> findByRoleAndVerificationStatus(Role role, VerificationStatus status);
    long countByRole(Role role);
    long countByRoleAndVerificationStatus(Role role, VerificationStatus status);
}
