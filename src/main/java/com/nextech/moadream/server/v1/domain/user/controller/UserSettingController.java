package com.nextech.moadream.server.v1.domain.user.controller;

import com.nextech.moadream.server.v1.domain.user.dto.UserSettingRequest;
import com.nextech.moadream.server.v1.domain.user.dto.UserSettingResponse;
import com.nextech.moadream.server.v1.domain.user.service.UserSettingService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Setting", description = "사용자 설정 관리 API")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserSettingController {

    private final UserSettingService userSettingService;

    @Operation(summary = "사용자 설정 조회", description = "사용자의 설정을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> getUserSetting(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        UserSettingResponse response = userSettingService.getUserSetting(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 설정 생성", description = "사용자의 설정을 생성합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> createUserSetting(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UserSettingRequest request) {
        UserSettingResponse response = userSettingService.createUserSetting(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예산 설정 수정", description = "월간 예산 및 알림 임계값을 수정합니다.")
    @PatchMapping("/users/{userId}/budget")
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateBudgetSettings(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UserSettingRequest request) {
        UserSettingResponse response = userSettingService.updateBudgetSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "알림 설정 수정", description = "푸시 및 이메일 알림 설정을 수정합니다.")
    @PatchMapping("/users/{userId}/notifications")
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateNotificationSettings(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UserSettingRequest request) {
        UserSettingResponse response = userSettingService.updateNotificationSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 설정 전체 수정", description = "사용자의 모든 설정을 수정합니다.")
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateUserSetting(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UserSettingRequest request) {
        UserSettingResponse response = userSettingService.updateUserSetting(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}