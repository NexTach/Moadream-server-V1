package com.nextech.moadream.server.v1.domain.user.repository;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserVerificationCode(String userVerificationCode);

    boolean existsByEmail(String email);

    boolean existsByUserVerificationCode(String userVerificationCode);
}