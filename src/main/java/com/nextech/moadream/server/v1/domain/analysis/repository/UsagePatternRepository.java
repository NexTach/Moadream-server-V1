package com.nextech.moadream.server.v1.domain.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

@Repository
public interface UsagePatternRepository extends JpaRepository<UsagePattern, Long> {

    List<UsagePattern> findByUser(User user);

    List<UsagePattern> findByUserAndUtilityType(User user, UtilityType utilityType);

    Optional<UsagePattern> findByUserAndUtilityTypeAndFrequencyType(User user, UtilityType utilityType,
            FrequencyType frequencyType);

    List<UsagePattern> findByUserAndFrequencyType(User user, FrequencyType frequencyType);
}
