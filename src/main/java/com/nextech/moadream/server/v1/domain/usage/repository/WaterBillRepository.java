package com.nextech.moadream.server.v1.domain.usage.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextech.moadream.server.v1.domain.usage.entity.WaterBill;
import com.nextech.moadream.server.v1.domain.user.entity.User;

public interface WaterBillRepository extends JpaRepository<WaterBill, Long> {

    List<WaterBill> findByUser(User user);

    List<WaterBill> findByUserAndIsPaid(User user, Boolean isPaid);

    Optional<WaterBill> findByUserAndBillingMonth(User user, LocalDate billingMonth);

    List<WaterBill> findByUserAndBillingMonthBetween(User user, LocalDate startMonth, LocalDate endMonth);

    List<WaterBill> findByUserOrderByBillingMonthDesc(User user);
}
