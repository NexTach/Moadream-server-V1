package com.nextech.moadream.server.v1.domain.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "절약 추천 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    @Schema(description = "추천 ID")
    private Long recId;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "유틸리티 타입")
    private UtilityType utilityType;

    @Schema(description = "추천 타입")
    private RecommendationType recType;

    @Schema(description = "추천 내용")
    private String recommendationText;

    @Schema(description = "예상 절감액")
    private BigDecimal expectedSavings;

    @Schema(description = "구현 난이도 (쉬움/보통/어려움)")
    private String implementationDifficulty;

    @Schema(description = "적용 여부")
    private Boolean isApplied;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    public static RecommendationResponse from(Recommendation recommendation) {
        return RecommendationResponse.builder().recId(recommendation.getRecId())
                .userId(recommendation.getUser().getUserId()).utilityType(recommendation.getUtilityType())
                .recType(recommendation.getRecType()).recommendationText(recommendation.getRecommendationText())
                .expectedSavings(recommendation.getExpectedSavings())
                .implementationDifficulty(recommendation.getImplementationDifficulty())
                .isApplied(recommendation.getIsApplied()).createdAt(recommendation.getCreatedAt()).build();
    }
}
