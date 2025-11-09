package com.nextech.moadream.server.v1.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserVerificationCode(String userVerificationCode);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
