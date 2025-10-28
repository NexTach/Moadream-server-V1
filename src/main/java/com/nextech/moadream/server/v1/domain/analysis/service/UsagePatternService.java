package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.analysis.dto.UsagePatternResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
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
public class UsagePatternService {

    private final UsagePatternRepository usagePatternRepository;
    private final UsageDataRepository usageDataRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<UsagePatternResponse> analyzeAndCreatePatterns(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return Stream.of(UtilityType.values())
                .flatMap(utilityType -> Stream.of(FrequencyType.values())
                        .map(frequencyType -> analyzePattern(user, utilityType, frequencyType)))
                .collect(Collectors.toList());
    }

    @Transactional
    public UsagePatternResponse analyzePattern(User user, UtilityType utilityType, FrequencyType frequencyType) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = getStartDateByFrequency(endDate, frequencyType);
        List<UsageData> usageDataList = usageDataRepository.findByUserAndUtilityType(user, utilityType).stream()
                .filter(data -> data.getMeasuredAt().isAfter(startDate) && data.getMeasuredAt().isBefore(endDate))
                .collect(Collectors.toList());
        if (usageDataList.isEmpty()) {
            log.info("No usage data found for user {} - {} - {}", user.getUserId(), utilityType, frequencyType);
            return null;
        }
        BigDecimal averageUsage = calculateAverage(usageDataList);
        BigDecimal peakUsage = calculatePeak(usageDataList);
        BigDecimal offPeakUsage = calculateOffPeak(usageDataList);
        String trend = analyzeTrend(usageDataList);
        UsagePattern pattern = usagePatternRepository
                .findByUserAndUtilityTypeAndFrequencyType(user, utilityType, frequencyType).orElse(null);
        if (pattern == null) {
            pattern = UsagePattern.builder().user(user).utilityType(utilityType).frequencyType(frequencyType)
                    .averageUsage(averageUsage).peakUsage(peakUsage).offPeakUsage(offPeakUsage).trend(trend).build();
            pattern = usagePatternRepository.save(pattern);
        } else {
            pattern.updatePattern(averageUsage, peakUsage, offPeakUsage, trend);
        }
        log.info("Pattern analyzed for user {} - {} - {}: avg={}, peak={}, trend={}", user.getUserId(), utilityType,
                frequencyType, averageUsage, peakUsage, trend);
        return UsagePatternResponse.from(pattern);
    }

    public List<UsagePatternResponse> getUserPatterns(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return usagePatternRepository.findByUser(user).stream().map(UsagePatternResponse::from)
                .collect(Collectors.toList());
    }

    public List<UsagePatternResponse> getUserPatternsByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return usagePatternRepository.findByUserAndUtilityType(user, utilityType).stream()
                .map(UsagePatternResponse::from).collect(Collectors.toList());
    }

    private LocalDateTime getStartDateByFrequency(LocalDateTime endDate, FrequencyType frequencyType) {
        return switch (frequencyType) {
            case DAILY -> endDate.minusDays(7);
            case WEEKLY -> endDate.minusWeeks(4);
            case MONTHLY -> endDate.minusMonths(3);
            case SEASONAL -> endDate.minusMonths(12);
        };
    }

    private BigDecimal calculateAverage(List<UsageData> usageDataList) {
        if (usageDataList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = usageDataList.stream().map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(usageDataList.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePeak(List<UsageData> usageDataList) {
        if (usageDataList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sortedUsages = usageDataList.stream().map(UsageData::getUsageAmount)
                .sorted((a, b) -> b.compareTo(a)).toList();
        int peakCount = Math.max(1, (int) (sortedUsages.size() * 0.2));
        return sortedUsages.stream().limit(peakCount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(peakCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOffPeak(List<UsageData> usageDataList) {
        if (usageDataList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sortedUsages = usageDataList.stream().map(UsageData::getUsageAmount).sorted()
                .toList();
        int offPeakCount = Math.max(1, (int) (sortedUsages.size() * 0.2));
        return sortedUsages.stream().limit(offPeakCount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(offPeakCount), 2, RoundingMode.HALF_UP);
    }

    private String analyzeTrend(List<UsageData> usageDataList) {
        if (usageDataList.size() < 2) {
            return "안정";
        }
        List<UsageData> sortedData = usageDataList.stream().sorted(Comparator.comparing(UsageData::getMeasuredAt))
                .collect(Collectors.toList());
        int halfSize = sortedData.size() / 2;
        BigDecimal firstHalfAvg = calculateAverage(sortedData.subList(0, halfSize));
        BigDecimal secondHalfAvg = calculateAverage(sortedData.subList(halfSize, sortedData.size()));
        BigDecimal diff = secondHalfAvg.subtract(firstHalfAvg);
        BigDecimal changePercent = diff.divide(firstHalfAvg, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (changePercent.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "증가";
        } else if (changePercent.compareTo(BigDecimal.valueOf(-10)) < 0) {
            return "감소";
        } else {
            return "안정";
        }
    }
}
