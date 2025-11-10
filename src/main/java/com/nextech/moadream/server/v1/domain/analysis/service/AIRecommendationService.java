package com.nextech.moadream.server.v1.domain.analysis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
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

    @Value("${spring.ai.openai.chat.enabled:false}")
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
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(pattern);

            ChatClient chatClient = chatClientBuilder.build();

            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

            String response = chatClient.prompt(prompt).call().content();

            log.info("AI response for user {}: {}", user.getUserId(), response);

            return parseAIResponse(response, pattern);
        } catch (Exception e) {
            log.error("Failed to generate AI recommendations for user {}", user.getUserId(), e);
            return new ArrayList<>();
        }
    }

    private String buildSystemPrompt() {
        return """
                당신은 한국전력공사(KEPCO)와 협력하는 에너지 절약 전문 컨설턴트입니다.

                당신의 역할:
                1. 사용자의 전기, 수도, 가스 사용 패턴을 분석합니다
                2. 실제 절감 효과가 있는 전문적인 절약 방안을 제시합니다
                3. 한국의 전기 요금제 구조(주택용, 경부하/중부하/최대부하 시간대)를 고려합니다
                4. 구체적인 금액을 원(₩) 단위로 추정합니다

                중요한 배경 지식:
                - 한국 전기요금: 경부하(23:00-09:00), 중부하(09:00-10:00, 12:00-13:00, 17:00-23:00), 최대부하(10:00-12:00, 13:00-17:00)
                - 주택용 누진제: 200kWh까지 기본, 201-400kWh 중간, 401kWh 이상 고가
                - 대기전력: 가구당 월평균 10-15% 전력 소비
                - 에너지 효율 1등급 가전: 5등급 대비 30-40% 절감
                - 절수 기기: 일반 대비 20-30% 물 절약

                응답 형식을 정확히 지켜주세요:
                각 추천은 반드시 다음 4줄로 구성되어야 합니다:
                1. 번호. [카테고리] 제목
                2. 설명: 구체적인 설명 (한 문장, ~해요/~세요 어체)
                3. 예상절감: 숫자만 (단위 없이, 원 단위)
                4. 난이도: 쉬움, 보통, 어려움 중 하나

                카테고리:
                - USAGE_REDUCTION: 사용량 줄이기
                - BEHAVIOR_CHANGE: 습관 개선
                - TIME_SHIFT: 시간대 이동 (전기요금 절감)
                - APPLIANCE_UPGRADE: 기기 교체
                - TARIFF_OPTIMIZATION: 요금제 최적화
                """;
    }

    private String buildUserPrompt(UsagePattern pattern) {
        String utilityName = getUtilityName(pattern.getUtilityType());
        String unit = getUnit(pattern.getUtilityType());

        return String.format("""
                다음 %s 사용 패턴을 분석하여 3가지 절약 방안을 제시해주세요.

                [사용 데이터]
                - 평균 사용량: %.2f %s
                - 피크 사용량: %.2f %s
                - 오프피크 사용량: %.2f %s
                - 사용 추세: %s

                아래 형식을 정확히 따라 작성해주세요. 다른 내용은 추가하지 마세요:

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

                주의사항:
                - 각 추천은 정확히 4줄로 작성하세요
                - 이모지, 마크다운 기호(**, ##), 추가 설명을 넣지 마세요
                - 예상절감은 숫자만 쓰세요 (원, ₩, 퍼센트 금지)
                - 실제 한국 전기요금 구조를 고려한 현실적인 금액을 제시하세요
                - 사용자의 실제 데이터를 반영하여 맞춤형 추천을 해주세요
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
            log.warn("Failed to parse AI response, returning empty list");
            return new ArrayList<>();
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
