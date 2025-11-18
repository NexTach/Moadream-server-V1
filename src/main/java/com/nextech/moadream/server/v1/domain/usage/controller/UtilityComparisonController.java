package com.nextech.moadream.server.v1.domain.usage.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.usage.dto.UtilityComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.service.UtilityComparisonService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Utility Comparison", description = "전기/수도/가스 통합 비교 API")
@RestController
@RequestMapping("/api/v1/bills/comparison")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UtilityComparisonController {

    private final UtilityComparisonService utilityComparisonService;

    @Operation(summary = "전월 대비 전기/수도/가스 통합 비교", description = "전월 대비 전기비, 수도비, 가스비의 각각의 증감률과 전체 증감률을 퍼센트로 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UtilityComparisonResponse>> compareAllUtilities(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "비교 기준 월 (현재 월)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentMonth) {
        UtilityComparisonResponse response = utilityComparisonService.compareAllUtilities(userId, currentMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
