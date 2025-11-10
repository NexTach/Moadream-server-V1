package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.analysis.dto.RecommendationResponse;
import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
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
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UsagePatternRepository usagePatternRepository;
    private final UserRepository userRepository;
    private final AIRecommendationService aiRecommendationService;

    @Transactional
    public List<RecommendationResponse> generateRecommendations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Recommendation> recommendations = new ArrayList<>();
        for (UtilityType utilityType : UtilityType.values()) {
            List<UsagePattern> patterns = usagePatternRepository.findByUserAndUtilityType(user, utilityType);
            if (patterns.isEmpty()) {
                continue;
            }
            patterns.stream().filter(p -> p.getFrequencyType() == FrequencyType.MONTHLY).findFirst().ifPresent(
                    monthlyPattern -> recommendations.addAll(generateRecommendationsFromPattern(user, monthlyPattern)));

        }
        recommendationRepository.deleteByUserAndIsApplied(user, false);
        List<Recommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);
        log.info("Generated {} recommendations for user {}", savedRecommendations.size(), userId);
        return savedRecommendations.stream().map(RecommendationResponse::from).collect(Collectors.toList());
    }

    private List<Recommendation> generateRecommendationsFromPattern(User user, UsagePattern pattern) {
        List<Recommendation> recommendations = new ArrayList<>();
        UtilityType utilityType = pattern.getUtilityType();

        // Try AI recommendations first
        try {
            List<AIRecommendationService.AIRecommendation> aiRecs = aiRecommendationService
                    .generateAIRecommendations(user, pattern);
            if (!aiRecs.isEmpty()) {
                log.info("Using AI-generated recommendations for user {}", user.getUserId());
                for (AIRecommendationService.AIRecommendation aiRec : aiRecs) {
                    recommendations.add(createRecommendation(user, utilityType, aiRec.getType(), aiRec.getText(),
                            aiRec.getExpectedSavings(), aiRec.getDifficulty()));
                }
                return recommendations;
            }
        } catch (Exception e) {
            log.warn("Failed to generate AI recommendations, falling back to rule-based", e);
        }

        // Fallback to rule-based recommendations
        log.info("Using rule-based recommendations for user {}", user.getUserId());
        if ("증가".equals(pattern.getTrend())) {
            recommendations.add(createRecommendation(user, utilityType, RecommendationType.USAGE_REDUCTION,
                    generateUsageReductionText(utilityType),
                    pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.15)), "보통"));
            recommendations.add(createRecommendation(user, utilityType, RecommendationType.BEHAVIOR_CHANGE,
                    generateBehaviorChangeText(utilityType),
                    pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.10)), "쉬움"));
        }
        if (pattern.getPeakUsage().compareTo(pattern.getAverageUsage().multiply(BigDecimal.valueOf(2))) > 0) {
            recommendations.add(createRecommendation(user, utilityType, RecommendationType.TIME_SHIFT,
                    generateTimeShiftText(utilityType), pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.20)),
                    "보통"));
        }
        if (utilityType == UtilityType.ELECTRICITY
                && pattern.getAverageUsage().compareTo(BigDecimal.valueOf(300)) > 0) {
            recommendations.add(createRecommendation(user, utilityType, RecommendationType.APPLIANCE_UPGRADE,
                    "에너지 효율 1등급 가전제품으로 교체하시면 장기적으로 전기료를 절감할 수 있어용.",
                    pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.25)), "어려움"));
        }
        recommendations.add(createRecommendation(user, utilityType, RecommendationType.TARIFF_OPTIMIZATION,
                generateTariffOptimizationText(utilityType),
                pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.08)), "쉬움"));
        return recommendations;
    }

    private Recommendation createRecommendation(User user, UtilityType utilityType, RecommendationType recType,
            String text, BigDecimal expectedSavings, String difficulty) {
        return Recommendation.builder().user(user).utilityType(utilityType).recType(recType).recommendationText(text)
                .expectedSavings(expectedSavings.setScale(2, RoundingMode.HALF_UP)).implementationDifficulty(difficulty)
                .isApplied(false).build();
    }

    private String generateUsageReductionText(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "최근 전기 사용량이 증가하고 있어용. 대기전력 차단과 불필요한 조명 끄기를 실천해보세용.";
            case WATER -> "최근 수도 사용량이 증가하고 있어용. 샤워 시간을 줄이고, 절수 기기 사용을 고려해보세용.";
            case GAS -> "최근 가스 사용량이 증가하고 있어용. 적정 온도 설정과 보일러 효율 점검을 추천드려용.";
        };
    }

    private String generateBehaviorChangeText(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "사용하지 않는 가전제품의 플러그를 뽑아두면 연간 10-15%의 전기료를 절감할 수 있어용.";
            case WATER -> "양치질이나 설거지 시 물을 받아서 사용하면 수도 사용량을 크게 줄일 수 있어용.";
            case GAS -> "외출 시 보일러를 외출 모드로 설정하고, 실내 온도를 2-3도 낮추면 가스 요금을 절감할 수 있어용.";
        };
    }

    private String generateTimeShiftText(UtilityType utilityType) {
        if (utilityType == UtilityType.ELECTRICITY) {
            return "전기 요금이 저렴한 경부하 시간대(23:00-09:00)에 세탁기, 식기세척기 등을 사용하면 요금을 절감할 수 있어용.";
        }
        return "피크 시간대를 피해 사용하면 요금을 절감할 수 있어용.";
    }

    private String generateTariffOptimizationText(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "현재 사용 패턴에 맞는 요금제로 변경하면 월 평균 8% 정도의 전기료를 절감할 수 있어용.";
            case WATER -> "누수 여부를 확인하고, 절수형 수도꼭지로 교체하면 수도 요금을 줄일 수 있어용.";
            case GAS -> "계절별 사용 패턴을 고려한 요금제로 변경하면 가스 요금을 절감할 수 있어용.";
        };
    }

    public List<RecommendationResponse> getUserRecommendations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return recommendationRepository.findByUser(user).stream().map(RecommendationResponse::from)
                .collect(Collectors.toList());
    }

    public List<RecommendationResponse> getUnappliedRecommendations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return recommendationRepository.findByUserAndIsApplied(user, false).stream().map(RecommendationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecommendationResponse markAsApplied(Long recId) {
        Recommendation recommendation = recommendationRepository.findById(recId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND));
        recommendation.markAsApplied();
        log.info("Recommendation {} marked as applied", recId);
        return RecommendationResponse.from(recommendation);
    }
}
