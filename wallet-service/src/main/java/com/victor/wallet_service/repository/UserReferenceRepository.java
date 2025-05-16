package com.victor.wallet_service.repository;

import com.victor.wallet_service.model.UserReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReferenceRepository extends JpaRepository<UserReference, String> {
    Optional<UserReference> findByEmail(String email);
    Optional<UserReference> findByPhoneNumber(String phoneNumber);
}
