package com.nextech.moadream.server.v1.domain.analysis.controller;

import com.nextech.moadream.server.v1.domain.analysis.dto.RecommendationResponse;
import com.nextech.moadream.server.v1.domain.analysis.service.RecommendationService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendation", description = "AI 기반 절약 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "AI 추천 생성", description = "사용 패턴을 분석하여 맞춤형 절약 추천을 생성합니다.")
    @PostMapping("/users/{userId}/generate")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> generateRecommendations(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<RecommendationResponse> response = recommendationService.generateRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 추천 조회", description = "사용자의 모든 추천을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getUserRecommendations(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<RecommendationResponse> response = recommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "미적용 추천 조회", description = "아직 적용하지 않은 추천을 조회합니다.")
    @GetMapping("/users/{userId}/unapplied")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getUnappliedRecommendations(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<RecommendationResponse> response = recommendationService.getUnappliedRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "추천 적용 처리", description = "추천을 적용 완료로 표시합니다.")
    @PatchMapping("/{recId}/apply")
    public ResponseEntity<ApiResponse<RecommendationResponse>> markAsApplied(
            @Parameter(description = "추천 ID") @PathVariable Long recId) {
        RecommendationResponse response = recommendationService.markAsApplied(recId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}