package com.nextech.moadream.server.v1.domain.usage.repository;

import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBillRepository extends JpaRepository<MonthlyBill, Long> {

    List<MonthlyBill> findByUser(User user);

    List<MonthlyBill> findByUserAndUtilityType(User user, UtilityType utilityType);

    Optional<MonthlyBill> findByUserAndUtilityTypeAndBillingMonth(
            User user, UtilityType utilityType, LocalDate billingMonth);

    List<MonthlyBill> findByUserAndBillingMonthBetween(User user, LocalDate startMonth, LocalDate endMonth);

    List<MonthlyBill> findByUserAndIsPaid(User user, Boolean isPaid);
}