package com.nextech.moadream.server.v1.domain.usage.controller;

import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.UsageDataResponse;
import com.nextech.moadream.server.v1.domain.usage.service.UsageDataService;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Usage Data", description = "사용량 데이터 관리 API")
@RestController
@RequestMapping("/api/v1/usage-data")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsageDataController {

    private final UsageDataService usageDataService;

    @Operation(summary = "사용량 데이터 등록", description = "새로운 사용량 데이터를 등록합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UsageDataResponse>> createUsageData(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UsageDataRequest request) {
        UsageDataResponse response = usageDataService.createUsageData(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 사용량 데이터 조회", description = "사용자의 모든 사용량 데이터를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UsageDataResponse>>> getUserUsageData(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<UsageDataResponse> response = usageDataService.getUserUsageData(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "유형별 사용량 데이터 조회", description = "특정 유형의 사용량 데이터를 조회합니다.")
    @GetMapping("/users/{userId}/type/{utilityType}")
    public ResponseEntity<ApiResponse<List<UsageDataResponse>>> getUserUsageDataByType(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @PathVariable UtilityType utilityType) {
        List<UsageDataResponse> response = usageDataService.getUserUsageDataByType(userId, utilityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "기간별 사용량 데이터 조회", description = "특정 기간의 사용량 데이터를 조회합니다.")
    @GetMapping("/users/{userId}/range")
    public ResponseEntity<ApiResponse<List<UsageDataResponse>>> getUserUsageDataByDateRange(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<UsageDataResponse> response = usageDataService.getUserUsageDataByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "최신 사용량 데이터 조회", description = "특정 유형의 최신 사용량 데이터를 조회합니다.")
    @GetMapping("/users/{userId}/latest/{utilityType}")
    public ResponseEntity<ApiResponse<UsageDataResponse>> getLatestUsageData(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @PathVariable UtilityType utilityType) {
        UsageDataResponse response = usageDataService.getLatestUsageData(userId, utilityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}