package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.analysis.dto.SavingsTrackingResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.entity.SavingsTracking;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.SavingsTrackingRepository;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsTrackingService {

    private final SavingsTrackingRepository savingsTrackingRepository;
    private final RecommendationRepository recommendationRepository;
    private final UsageDataRepository usageDataRepository;
    private final UserRepository userRepository;

    @Transactional
    public SavingsTrackingResponse startTracking(Long userId, Long recId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Recommendation recommendation = recommendationRepository.findById(recId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));

        if (!recommendation.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        BigDecimal baselineCost = calculateMonthlyUsage(user, recommendation.getUtilityType(), lastMonth);

        YearMonth currentMonth = YearMonth.now();
        SavingsTracking tracking = SavingsTracking.builder().user(user).recommendation(recommendation)
                .utilityType(recommendation.getUtilityType()).trackingMonth(currentMonth.atDay(1))
                .actualUsage(BigDecimal.ZERO).baselineCost(baselineCost).actualCost(BigDecimal.ZERO)
                .savingsAchieved(BigDecimal.ZERO).build();

        tracking = savingsTrackingRepository.save(tracking);

        log.info("Started tracking for recommendation {} for user {}", recId, userId);

        return SavingsTrackingResponse.from(tracking);
    }

    @Transactional
    public SavingsTrackingResponse updateTracking(Long trackingId) {
        SavingsTracking tracking = savingsTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVINGS_TRACKING_NOT_FOUND));

        YearMonth currentMonth = YearMonth.from(tracking.getTrackingMonth());
        BigDecimal actualCost = calculateMonthlyUsage(tracking.getUser(), tracking.getUtilityType(), currentMonth);

        BigDecimal actualUsage = calculateMonthlyUsageAmount(tracking.getUser(), tracking.getUtilityType(),
                currentMonth);

        BigDecimal savingsAchieved = tracking.getBaselineCost().subtract(actualCost);

        tracking = SavingsTracking.builder().user(tracking.getUser()).recommendation(tracking.getRecommendation())
                .utilityType(tracking.getUtilityType()).trackingMonth(tracking.getTrackingMonth())
                .actualUsage(actualUsage).baselineCost(tracking.getBaselineCost()).actualCost(actualCost)
                .savingsAchieved(savingsAchieved).build();

        tracking = savingsTrackingRepository.save(tracking);

        log.info("Updated tracking {} - baseline: {}, actual: {}, savings: {}", trackingId, tracking.getBaselineCost(),
                actualCost, savingsAchieved);

        return SavingsTrackingResponse.from(tracking);
    }

    public List<SavingsTrackingResponse> getUserTrackings(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return savingsTrackingRepository.findByUser(user).stream().map(SavingsTrackingResponse::from)
                .collect(Collectors.toList());
    }

    public List<SavingsTrackingResponse> getTrackingsByPeriod(Long userId, LocalDate startMonth, LocalDate endMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return savingsTrackingRepository.findByUserAndTrackingMonthBetween(user, startMonth, endMonth).stream()
                .map(SavingsTrackingResponse::from).collect(Collectors.toList());
    }

    public BigDecimal getTotalSavings(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return savingsTrackingRepository.findByUser(user).stream().map(SavingsTracking::getSavingsAchieved)
                .filter(savings -> savings != null && savings.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyUsage(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> monthlyData = usageDataRepository.findByUserAndMeasuredAtBetween(user, startOfMonth,
                endOfMonth);

        return monthlyData.stream()
                .filter(data -> data.getUtilityType() == utilityType && data.getCurrentCharge() != null)
                .map(UsageData::getCurrentCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyUsageAmount(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> monthlyData = usageDataRepository.findByUserAndMeasuredAtBetween(user, startOfMonth,
                endOfMonth);

        return monthlyData.stream().filter(data -> data.getUtilityType() == utilityType).map(UsageData::getUsageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
