package com.nextech.moadream.server.v1.domain.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByUser(User user);

    List<Recommendation> findByUserAndUtilityType(User user, UtilityType utilityType);

    List<Recommendation> findByUserAndIsApplied(User user, Boolean isApplied);

    List<Recommendation> findByUserAndRecType(User user, RecommendationType recType);

    List<Recommendation> findByUserOrderByExpectedSavingsDesc(User user);

    void deleteByUserAndIsApplied(User user, Boolean isApplied);
}
