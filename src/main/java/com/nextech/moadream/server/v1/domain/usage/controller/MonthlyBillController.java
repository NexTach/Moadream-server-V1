package com.nextech.moadream.server.v1.domain.usage.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.usage.dto.AllUtilitiesComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillStatisticsResponse;
import com.nextech.moadream.server.v1.domain.usage.service.MonthlyBillService;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Monthly Bill", description = "월간 청구서 관리 API")
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MonthlyBillController {

    private final MonthlyBillService monthlyBillService;

    @Operation(summary = "청구서 생성", description = "새로운 청구서를 생성합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<MonthlyBillResponse>> createBill(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody MonthlyBillRequest request) {
        MonthlyBillResponse response = monthlyBillService.createBill(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 청구서 조회", description = "사용자의 모든 청구서를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<MonthlyBillResponse>>> getUserBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<MonthlyBillResponse> response = monthlyBillService.getUserBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "유형별 청구서 조회", description = "특정 유형의 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/type/{utilityType}")
    public ResponseEntity<ApiResponse<List<MonthlyBillResponse>>> getUserBillsByType(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @PathVariable UtilityType utilityType) {
        List<MonthlyBillResponse> response = monthlyBillService.getUserBillsByType(userId, utilityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월별 청구서 조회", description = "특정 월의 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/month")
    public ResponseEntity<ApiResponse<MonthlyBillResponse>> getBillByMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @RequestParam UtilityType utilityType,
            @Parameter(description = "청구 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth) {
        MonthlyBillResponse response = monthlyBillService.getBillByMonth(userId, utilityType, billingMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "미납 청구서 조회", description = "사용자의 미납 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/unpaid")
    public ResponseEntity<ApiResponse<List<MonthlyBillResponse>>> getUnpaidBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<MonthlyBillResponse> response = monthlyBillService.getUnpaidBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "청구서 납부 처리", description = "청구서를 납부 완료로 표시합니다.")
    @PatchMapping("/{billId}/pay")
    public ResponseEntity<ApiResponse<MonthlyBillResponse>> markBillAsPaid(
            @Parameter(description = "청구서 ID") @PathVariable Long billId) {
        MonthlyBillResponse response = monthlyBillService.markBillAsPaid(billId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "청구서 통계 조회", description = "특정 기간의 청구서 통계를 조회합니다.")
    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<ApiResponse<MonthlyBillStatisticsResponse>> getBillStatistics(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startMonth,
            @Parameter(description = "종료 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endMonth) {
        MonthlyBillStatisticsResponse response = monthlyBillService.getBillStatistics(userId, startMonth, endMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전월 대비 청구서 비교 (특정 유형)", description = "특정 유형(전기/수도/가스)의 전월 대비 사용량 및 요금 증감률을 조회합니다.")
    @GetMapping("/users/{userId}/compare/{utilityType}")
    public ResponseEntity<ApiResponse<MonthlyBillComparisonResponse>> compareWithPreviousMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "사용량 유형") @PathVariable UtilityType utilityType,
            @Parameter(description = "비교 기준 월 (현재 월)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentMonth) {
        MonthlyBillComparisonResponse response = monthlyBillService.compareWithPreviousMonth(userId, utilityType,
                currentMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전월 대비 전체 유형 청구서 비교", description = "전기, 수도, 가스, 인터넷, 모바일 전체 유형의 전월 대비 증감률을 한 번에 조회합니다.")
    @GetMapping("/users/{userId}/compare-all")
    public ResponseEntity<ApiResponse<AllUtilitiesComparisonResponse>> compareAllUtilitiesWithPreviousMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "비교 기준 월 (현재 월)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentMonth) {
        AllUtilitiesComparisonResponse response = monthlyBillService.compareAllUtilitiesWithPreviousMonth(userId,
                currentMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
