package com.nextech.moadream.server.v1.domain.usage.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

@Repository
public interface UsageDataRepository extends JpaRepository<UsageData, Long> {

    List<UsageData> findByUser(User user);

    List<UsageData> findByUserAndUtilityType(User user, UtilityType utilityType);

    List<UsageData> findByUserAndMeasuredAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    List<UsageData> findByUserAndUtilityTypeAndMeasuredAtBetween(User user, UtilityType utilityType,
            LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT u FROM UsageData u WHERE u.user = :user AND u.utilityType = :utilityType "
            + "ORDER BY u.measuredAt DESC LIMIT 1")
    UsageData findLatestByUserAndUtilityType(@Param("user") User user, @Param("utilityType") UtilityType utilityType);
}
