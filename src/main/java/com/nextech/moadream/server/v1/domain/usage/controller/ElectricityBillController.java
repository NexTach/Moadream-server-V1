package com.nextech.moadream.server.v1.domain.usage.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.usage.dto.ElectricityBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.ElectricityBillResponse;
import com.nextech.moadream.server.v1.domain.usage.service.ElectricityBillService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Electricity Bill", description = "전기 청구서 관리 API")
@RestController
@RequestMapping("/api/v1/bills/electricity")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ElectricityBillController {

    private final ElectricityBillService electricityBillService;

    @Operation(summary = "전기 청구서 생성", description = "새로운 전기 청구서를 생성합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<ElectricityBillResponse>> createBill(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody ElectricityBillRequest request) {
        ElectricityBillResponse response = electricityBillService.createBill(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 전기 청구서 조회", description = "사용자의 모든 전기 청구서를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<ElectricityBillResponse>>> getUserBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<ElectricityBillResponse> response = electricityBillService.getUserBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월별 전기 청구서 조회", description = "특정 월의 전기 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/month")
    public ResponseEntity<ApiResponse<ElectricityBillResponse>> getBillByMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "청구 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth) {
        ElectricityBillResponse response = electricityBillService.getBillByMonth(userId, billingMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "미납 전기 청구서 조회", description = "사용자의 미납 전기 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/unpaid")
    public ResponseEntity<ApiResponse<List<ElectricityBillResponse>>> getUnpaidBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<ElectricityBillResponse> response = electricityBillService.getUnpaidBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전기 청구서 납부 처리", description = "전기 청구서를 납부 완료로 표시합니다.")
    @PatchMapping("/{billId}/pay")
    public ResponseEntity<ApiResponse<ElectricityBillResponse>> markBillAsPaid(
            @Parameter(description = "청구서 ID") @PathVariable Long billId) {
        ElectricityBillResponse response = electricityBillService.markBillAsPaid(billId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전월 대비 전기비 증감률 조회", description = "전월 대비 전기비 증감률을 퍼센트로 조회합니다.")
    @GetMapping("/users/{userId}/compare")
    public ResponseEntity<ApiResponse<BigDecimal>> compareWithPreviousMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "비교 기준 월 (현재 월)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentMonth) {
        BigDecimal changeRate = electricityBillService.compareWithPreviousMonth(userId, currentMonth);
        return ResponseEntity.ok(ApiResponse.success(changeRate));
    }
}
