package com.nextech.moadream.server.v1.domain.analysis.controller;

import com.nextech.moadream.server.v1.domain.analysis.dto.SavingsTrackingResponse;
import com.nextech.moadream.server.v1.domain.analysis.service.SavingsTrackingService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Savings Tracking", description = "절감 효과 추적 API")
@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SavingsTrackingController {

    private final SavingsTrackingService savingsTrackingService;

    @Operation(summary = "절감 추적 시작", description = "특정 추천에 대한 절감 효과 추적을 시작합니다.")
    @PostMapping("/users/{userId}/recommendations/{recId}/start")
    public ResponseEntity<ApiResponse<SavingsTrackingResponse>> startTracking(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "추천 ID") @PathVariable Long recId) {
        SavingsTrackingResponse response = savingsTrackingService.startTracking(userId, recId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "절감 추적 업데이트", description = "절감 효과를 현재 사용량 기준으로 업데이트합니다.")
    @PatchMapping("/{trackingId}/update")
    public ResponseEntity<ApiResponse<SavingsTrackingResponse>> updateTracking(
            @Parameter(description = "추적 ID") @PathVariable Long trackingId) {
        SavingsTrackingResponse response = savingsTrackingService.updateTracking(trackingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 절감 추적 조회", description = "사용자의 모든 절감 추적 내역을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<SavingsTrackingResponse>>> getUserTrackings(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<SavingsTrackingResponse> response = savingsTrackingService.getUserTrackings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "기간별 절감 추적 조회", description = "특정 기간의 절감 추적 내역을 조회합니다.")
    @GetMapping("/users/{userId}/period")
    public ResponseEntity<ApiResponse<List<SavingsTrackingResponse>>> getTrackingsByPeriod(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startMonth,
            @Parameter(description = "종료 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endMonth) {
        List<SavingsTrackingResponse> response = savingsTrackingService.getTrackingsByPeriod(userId, startMonth, endMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "총 절감액 조회", description = "사용자의 총 절감액을 조회합니다.")
    @GetMapping("/users/{userId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalSavings(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        BigDecimal totalSavings = savingsTrackingService.getTotalSavings(userId);
        return ResponseEntity.ok(ApiResponse.success(totalSavings));
    }
}