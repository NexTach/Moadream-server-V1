package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.ollama.chat.enabled:false}")
    private boolean aiEnabled;

    public static class AIRecommendation {
        private final String text;
        private final RecommendationType type;
        private final BigDecimal expectedSavings;
        private final String difficulty;

        public AIRecommendation(String text, RecommendationType type, BigDecimal expectedSavings, String difficulty) {
            this.text = text;
            this.type = type;
            this.expectedSavings = expectedSavings;
            this.difficulty = difficulty;
        }

        public String getText() {
            return text;
        }

        public RecommendationType getType() {
            return type;
        }

        public BigDecimal getExpectedSavings() {
            return expectedSavings;
        }

        public String getDifficulty() {
            return difficulty;
        }
    }

    public List<AIRecommendation> generateAIRecommendations(User user, UsagePattern pattern) {
        if (!aiEnabled) {
            log.info("AI recommendations disabled, returning empty list");
            return new ArrayList<>();
        }

        try {
            String prompt = buildPrompt(pattern);
            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient.prompt().user(prompt).call().content();

            log.info("AI response for user {}: {}", user.getUserId(), response);

            return parseAIResponse(response, pattern);
        } catch (Exception e) {
            log.error("Failed to generate AI recommendations for user {}", user.getUserId(), e);
            return new ArrayList<>();
        }
    }

    private String buildPrompt(UsagePattern pattern) {
        String utilityName = getUtilityName(pattern.getUtilityType());
        String unit = getUnit(pattern.getUtilityType());

        return String.format("""
                당신은 에너지 절약 전문가입니다. 다음 사용 패턴을 분석하고 절약 방안을 제안해주세요.

                [사용 패턴 분석]
                유틸리티: %s
                평균 사용량: %.2f %s
                피크 사용량: %.2f %s
                오프피크 사용량: %.2f %s
                추세: %s

                다음 형식으로 정확히 3가지 절약 방안을 제안해주세요. 각 방안은 반드시 아래 형식을 따라야 합니다:

                1. [카테고리] 제목
                설명: (구체적인 실행 방법을 한 문장으로)
                예상절감: (숫자만, 원 단위 생략) 원
                난이도: (쉬움/보통/어려움 중 하나)

                카테고리는 다음 중 하나를 선택:
                - USAGE_REDUCTION (사용량 절감)
                - BEHAVIOR_CHANGE (행동 변화)
                - TIME_SHIFT (시간대 이동)
                - APPLIANCE_UPGRADE (기기 교체)
                - TARIFF_OPTIMIZATION (요금제 최적화)

                예시:
                1. [USAGE_REDUCTION] 대기전력 차단으로 전기 절약
                설명: 사용하지 않는 가전제품의 플러그를 뽑거나 멀티탭을 사용하여 대기전력을 차단하세요.
                예상절감: 15000
                난이도: 쉬움
                """,
                utilityName,
                pattern.getAverageUsage(), unit,
                pattern.getPeakUsage(), unit,
                pattern.getOffPeakUsage(), unit,
                pattern.getTrend()
        );
    }

    private List<AIRecommendation> parseAIResponse(String response, UsagePattern pattern) {
        List<AIRecommendation> recommendations = new ArrayList<>();

        Pattern recPattern = Pattern.compile(
                "\\d+\\.\\s*\\[([A-Z_]+)\\]\\s*(.+?)\\n\\s*설명:\\s*(.+?)\\n\\s*예상절감:\\s*([\\d,]+)\\s*원?\\n\\s*난이도:\\s*(.+?)(?=\\n\\n|\\n\\d+\\.|$)",
                Pattern.DOTALL
        );

        Matcher matcher = recPattern.matcher(response);

        while (matcher.find() && recommendations.size() < 3) {
            try {
                String typeStr = matcher.group(1).trim();
                String title = matcher.group(2).trim();
                String description = matcher.group(3).trim();
                String savingsStr = matcher.group(4).trim().replaceAll(",", "");
                String difficulty = matcher.group(5).trim();

                RecommendationType type = parseRecommendationType(typeStr);
                BigDecimal expectedSavings = new BigDecimal(savingsStr);

                String fullText = title + ". " + description;

                recommendations.add(new AIRecommendation(fullText, type, expectedSavings, difficulty));

                log.debug("Parsed recommendation: type={}, savings={}, difficulty={}", type, expectedSavings, difficulty);
            } catch (Exception e) {
                log.warn("Failed to parse recommendation: {}", matcher.group(0), e);
            }
        }

        if (recommendations.isEmpty()) {
            log.warn("Failed to parse AI response, using fallback recommendations");
            return getFallbackRecommendations(pattern);
        }

        return recommendations;
    }

    private RecommendationType parseRecommendationType(String typeStr) {
        try {
            return RecommendationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown recommendation type: {}, defaulting to USAGE_REDUCTION", typeStr);
            return RecommendationType.USAGE_REDUCTION;
        }
    }

    private List<AIRecommendation> getFallbackRecommendations(UsagePattern pattern) {
        List<AIRecommendation> recommendations = new ArrayList<>();
        UtilityType utilityType = pattern.getUtilityType();

        recommendations.add(new AIRecommendation(
                getFallbackText(utilityType),
                RecommendationType.USAGE_REDUCTION,
                pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.10)),
                "보통"
        ));

        return recommendations;
    }

    private String getFallbackText(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "AI 분석이 일시적으로 불가능합니다. 대기전력 차단과 에너지 효율 가전 사용을 권장합니다.";
            case WATER -> "AI 분석이 일시적으로 불가능합니다. 절수 기기 설치와 물 재사용을 권장합니다.";
            case GAS -> "AI 분석이 일시적으로 불가능합니다. 적정 온도 유지와 보일러 효율 점검을 권장합니다.";
        };
    }

    private String getUtilityName(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "전기";
            case WATER -> "수도";
            case GAS -> "가스";
        };
    }

    private String getUnit(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "kWh";
            case WATER, GAS -> "m³";
        };
    }
}