package com.nextech.moadream.server.v1.domain.usage.repository;

import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageAlertRepository extends JpaRepository<UsageAlert, Long> {

    List<UsageAlert> findByUser(User user);

    List<UsageAlert> findByUserAndIsRead(User user, Boolean isRead);

    List<UsageAlert> findByUserAndUtilityType(User user, UtilityType utilityType);

    List<UsageAlert> findByUserAndAlertType(User user, AlertType alertType);

    List<UsageAlert> findByUserOrderByCreatedAtDesc(User user);
}