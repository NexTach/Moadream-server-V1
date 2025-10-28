package com.nextech.moadream.server.v1.domain.privacy.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.privacy.entity.PrivacyLog;
import com.nextech.moadream.server.v1.domain.user.entity.User;

@Repository
public interface PrivacyLogRepository extends JpaRepository<PrivacyLog, Long> {
    List<PrivacyLog> findByUser(User user);
    @Query("SELECT p FROM PrivacyLog p WHERE p.deletionScheduledAt <= :now AND p.isDeleted = false")
    List<PrivacyLog> findScheduledForDeletion(LocalDateTime now);
}
