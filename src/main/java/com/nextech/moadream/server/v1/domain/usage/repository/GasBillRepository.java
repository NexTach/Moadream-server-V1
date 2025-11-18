package com.nextech.moadream.server.v1.domain.usage.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nextech.moadream.server.v1.domain.usage.entity.GasBill;
import com.nextech.moadream.server.v1.domain.user.entity.User;

public interface GasBillRepository extends JpaRepository<GasBill, Long> {

    List<GasBill> findByUser(User user);

    List<GasBill> findByUserAndIsPaid(User user, Boolean isPaid);

    Optional<GasBill> findByUserAndBillingMonth(User user, LocalDate billingMonth);

    List<GasBill> findByUserAndBillingMonthBetween(User user, LocalDate startMonth, LocalDate endMonth);

    List<GasBill> findByUserOrderByBillingMonthDesc(User user);
}
