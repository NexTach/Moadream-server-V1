package com.nextech.moadream.server.v1.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserBill;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

@Repository
public interface UserBillRepository extends JpaRepository<UserBill, Long> {

    List<UserBill> findByUser(User user);

    List<UserBill> findByUserAndUtilityType(User user, UtilityType utilityType);

    Optional<UserBill> findByBillNumber(String billNumber);

    boolean existsByBillNumber(String billNumber);

    List<UserBill> findByUserAndIsVerified(User user, Boolean isVerified);
}
