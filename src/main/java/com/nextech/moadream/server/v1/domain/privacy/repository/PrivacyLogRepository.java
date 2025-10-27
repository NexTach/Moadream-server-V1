package com.nextech.moadream.server.v1.domain.privacy.repository;

import com.nextech.moadream.server.v1.domain.privacy.entity.PrivacyLog;
import com.nextech.moadream.server.v1.domain.privacy.enums.ActionType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrivacyLogRepository extends JpaRepository<PrivacyLog, Long> {

    List<PrivacyLog> findByUser(User user);

    List<PrivacyLog> findByUserAndActionType(User user, ActionType actionType);

    List<PrivacyLog> findByUserAndIsDeleted(User user, Boolean isDeleted);

    @Query("SELECT p FROM PrivacyLog p WHERE p.deletionScheduledAt <= :now AND p.isDeleted = false")
    List<PrivacyLog> findScheduledForDeletion(LocalDateTime now);
}