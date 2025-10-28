package com.nextech.moadream.server.v1.domain.analysis.controller;

import com.nextech.moadream.server.v1.domain.analysis.dto.UsagePatternResponse;
import com.nextech.moadream.server.v1.domain.analysis.service.UsagePatternService;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usage Pattern", description = "사용 패턴 분석 API")
@RestController
@RequestMapping("/api/v1/patterns")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsagePatternController {

    private final UsagePatternService usagePatternService;

    @Operation(summary = "사용 패턴 분석 및 생성", description = "사용자의 사용 데이터를 분석하여 패턴을 생성합니다.")
    @PostMapping("/users/{userId}/analyze")
    public ResponseEntity<ApiResponse<List<UsagePatternResponse>>> analyzePatterns(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<UsagePatternResponse> response = usagePatternService.analyzeAndCreatePatterns(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 패턴 조회", description = "사용자의 모든 사용 패턴을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UsagePatternResponse>>> getUserPatterns(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<UsagePatternResponse> response = usagePatternService.getUserPatterns(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "유틸리티 타입별 패턴 조회", description = "특정 유틸리티 타입의 패턴을 조회합니다.")
    @GetMapping("/users/{userId}/type/{utilityType}")
    public ResponseEntity<ApiResponse<List<UsagePatternResponse>>> getUserPatternsByType(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "유틸리티 타입") @PathVariable UtilityType utilityType) {
        List<UsagePatternResponse> response = usagePatternService.getUserPatternsByType(userId, utilityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}