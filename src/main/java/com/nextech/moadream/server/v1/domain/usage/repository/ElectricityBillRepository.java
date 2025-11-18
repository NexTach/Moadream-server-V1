package com.nextech.moadream.server.v1.domain.usage.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextech.moadream.server.v1.domain.usage.entity.ElectricityBill;
import com.nextech.moadream.server.v1.domain.user.entity.User;

public interface ElectricityBillRepository extends JpaRepository<ElectricityBill, Long> {

    List<ElectricityBill> findByUser(User user);

    List<ElectricityBill> findByUserAndIsPaid(User user, Boolean isPaid);

    Optional<ElectricityBill> findByUserAndBillingMonth(User user, LocalDate billingMonth);

    List<ElectricityBill> findByUserAndBillingMonthBetween(User user, LocalDate startMonth, LocalDate endMonth);

    List<ElectricityBill> findByUserOrderByBillingMonthDesc(User user);
}
