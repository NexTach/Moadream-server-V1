package com.nextech.moadream.server.v1.domain.analysis.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.analysis.entity.SavingsTracking;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

@Repository
public interface SavingsTrackingRepository extends JpaRepository<SavingsTracking, Long> {

    List<SavingsTracking> findByUser(User user);

    List<SavingsTracking> findByUserAndUtilityType(User user, UtilityType utilityType);

    List<SavingsTracking> findByUserAndTrackingMonthBetween(User user, LocalDate startMonth, LocalDate endMonth);

    @Query("SELECT SUM(s.savingsAchieved) FROM SavingsTracking s WHERE s.user = :user")
    BigDecimal calculateTotalSavings(@Param("user") User user);

    @Query("SELECT SUM(s.savingsAchieved) FROM SavingsTracking s WHERE s.user = :user AND s.utilityType = :utilityType")
    BigDecimal calculateTotalSavingsByUtilityType(@Param("user") User user,
            @Param("utilityType") UtilityType utilityType);
}
