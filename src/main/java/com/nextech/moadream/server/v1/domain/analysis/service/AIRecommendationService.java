package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.ollama.chat.enabled:false}")
    private boolean aiEnabled;

    @Getter
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
                당신은 친근하고 따뜻한 에너지 절약 도우미예요. 다음 사용 패턴을 살펴보고 부담 없이 실천할 수 있는 절약 방안을 제안해주세요.

                [사용 패턴 분석]
                유틸리티: %s
                평균 사용량: %.2f %s
                피크 사용량: %.2f %s
                오프피크 사용량: %.2f %s
                추세: %s

                아래 예시 형식을 그대로 따라서 3가지 절약 방안을 작성해주세요. 다른 내용은 절대 추가하지 마세요:

                1. [USAGE_REDUCTION] 대기전력 차단으로 전기 절약하기
                설명: 사용하지 않는 가전제품은 플러그를 뽑거나 멀티탭을 활용해서 대기전력을 차단해보세요.
                예상절감: 15000
                난이도: 쉬움

                2. [BEHAVIOR_CHANGE] 조명 사용 습관 개선하기
                설명: 낮 시간에는 자연광을 활용하고 불필요한 조명은 꺼서 전기를 절약해보세요.
                예상절감: 12000
                난이도: 쉬움

                3. [TIME_SHIFT] 경부하 시간대 활용하기
                설명: 세탁기나 식기세척기는 밤 11시 이후에 사용해서 전기 요금을 절약해보세요.
                예상절감: 18000
                난이도: 보통

                작성 규칙 (반드시 지켜주세요):
                1. 제목 앞에 번호와 대괄호 안에 카테고리를 정확히 쓰세요 (예: 1. [USAGE_REDUCTION])
                2. 설명은 "설명:" 다음에 한 문장으로 쓰고 '~해요', '~세요', '~요' 어체를 사용하세요
                3. 예상절감은 "예상절감:" 다음에 숫자만 쓰세요 (원, 퍼센트 같은 단위 절대 금지)
                4. 난이도는 "난이도:" 다음에 '쉬움', '보통', '어려움' 중 하나만 쓰세요
                5. 이모지, **, 추가 설명, 제목은 절대 추가하지 마세요
                6. 위 예시처럼 정확히 4줄씩 작성하세요

                카테고리 선택지: USAGE_REDUCTION, BEHAVIOR_CHANGE, TIME_SHIFT, APPLIANCE_UPGRADE, TARIFF_OPTIMIZATION
                """, utilityName, pattern.getAverageUsage(), unit, pattern.getPeakUsage(), unit,
                pattern.getOffPeakUsage(), unit, pattern.getTrend());
    }

    private List<AIRecommendation> parseAIResponse(String response, UsagePattern pattern) {
        List<AIRecommendation> recommendations = new ArrayList<>();

        Pattern recPattern = Pattern.compile(
                "\\d+\\.\\s*\\[([A-Z_]+)]\\s*(.+?)\\n\\s*설명:\\s*(.+?)\\n\\s*예상절감:\\s*([\\d,]+)\\s*원?\\n\\s*난이도:\\s*(.+?)(?=\\n\\n|\\n\\d+\\.|$)",
                Pattern.DOTALL);

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

                log.debug("Parsed recommendation: type={}, savings={}, difficulty={}", type, expectedSavings,
                        difficulty);
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

        recommendations.add(new AIRecommendation(getFallbackText(utilityType), RecommendationType.USAGE_REDUCTION,
                pattern.getAverageUsage().multiply(BigDecimal.valueOf(0.10)), "보통"));

        return recommendations;
    }

    private String getFallbackText(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "AI 분석이 일시적으로 어려워요. 대기전력 차단과 에너지 효율 가전 사용을 추천해드려요.";
            case WATER -> "AI 분석이 일시적으로 어려워요. 절수 기기 설치와 물 재사용을 추천해드려요.";
            case GAS -> "AI 분석이 일시적으로 어려워요. 적정 온도 유지와 보일러 효율 점검을 추천해드려요.";
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
