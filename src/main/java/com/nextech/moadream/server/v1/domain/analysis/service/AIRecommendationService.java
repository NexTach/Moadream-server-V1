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
    private final com.nextech.moadream.server.v1.domain.chat.service.PromptTemplateService promptTemplateService;

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
            String systemPrompt = promptTemplateService.getPrompt("AI_RECOMMENDATION_SYSTEM");
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

    private String buildUserPrompt(UsagePattern pattern) {
        String utilityName = getUtilityName(pattern.getUtilityType());
        String unit = getUnit(pattern.getUtilityType());

        java.util.Map<String, String> variables = new java.util.HashMap<>();
        variables.put("UTILITY_NAME", utilityName);
        variables.put("AVERAGE_USAGE", String.format("%.2f", pattern.getAverageUsage()));
        variables.put("UNIT", unit);
        variables.put("PEAK_USAGE", String.format("%.2f", pattern.getPeakUsage()));
        variables.put("OFF_PEAK_USAGE", String.format("%.2f", pattern.getOffPeakUsage()));
        variables.put("TREND", pattern.getTrend());

        return promptTemplateService.buildPrompt("AI_RECOMMENDATION_USER", variables);
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
