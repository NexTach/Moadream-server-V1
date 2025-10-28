package com.nextech.moadream.server.v1.domain.usage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.usage.dto.UsageAlertRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageAlertResponse;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.usage.service.UsageAlertService;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usage Alert", description = "사용량 알림 관리 API")
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsageAlertController {

    private final UsageAlertService usageAlertService;

    @Operation(summary = "알림 생성", description = "새로운 알림을 생성합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UsageAlertResponse>> createAlert(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UsageAlertRequest request) {
        UsageAlertResponse response = usageAlertService.createAlert(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 알림 조회", description = "사용자의 모든 알림을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UsageAlertResponse>>> getUserAlerts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<UsageAlertResponse> response = usageAlertService.getUserAlerts(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "미읽음 알림 조회", description = "사용자의 미읽음 알림을 조회합니다.")
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<ApiResponse<List<UsageAlertResponse>>> getUnreadAlerts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<UsageAlertResponse> response = usageAlertService.getUnreadAlerts(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "유형별 알림 조회", description = "특정 유형의 알림을 조회합니다.")
    @GetMapping("/users/{userId}/type/{utilityType}")
    public ResponseEntity<ApiResponse<List<UsageAlertResponse>>> getAlertsByType(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @PathVariable UtilityType utilityType) {
        List<UsageAlertResponse> response = usageAlertService.getAlertsByType(userId, utilityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "알림 타입별 조회", description = "특정 알림 타입의 알림을 조회합니다.")
    @GetMapping("/users/{userId}/alert-type/{alertType}")
    public ResponseEntity<ApiResponse<List<UsageAlertResponse>>> getAlertsByAlertType(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "알림 타입") @PathVariable AlertType alertType) {
        List<UsageAlertResponse> response = usageAlertService.getAlertsByAlertType(userId, alertType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음으로 표시합니다.")
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<UsageAlertResponse>> markAlertAsRead(
            @Parameter(description = "알림 ID") @PathVariable Long alertId) {
        UsageAlertResponse response = usageAlertService.markAlertAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 미읽음 알림을 읽음으로 표시합니다.")
    @PatchMapping("/users/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAlertsAsRead(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        usageAlertService.markAllAlertsAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
