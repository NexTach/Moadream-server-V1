package com.nextech.moadream.server.v1.domain.usage.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nextech.moadream.server.v1.domain.usage.dto.WaterBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.WaterBillResponse;
import com.nextech.moadream.server.v1.domain.usage.service.WaterBillService;
import com.nextech.moadream.server.v1.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Water Bill", description = "수도 청구서 관리 API")
@RestController
@RequestMapping("/api/v1/bills/water")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WaterBillController {

    private final WaterBillService waterBillService;

    @Operation(summary = "수도 청구서 생성", description = "새로운 수도 청구서를 생성합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<WaterBillResponse>> createBill(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody WaterBillRequest request) {
        WaterBillResponse response = waterBillService.createBill(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자 수도 청구서 조회", description = "사용자의 모든 수도 청구서를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<WaterBillResponse>>> getUserBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<WaterBillResponse> response = waterBillService.getUserBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월별 수도 청구서 조회", description = "특정 월의 수도 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/month")
    public ResponseEntity<ApiResponse<WaterBillResponse>> getBillByMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "청구 월") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth) {
        WaterBillResponse response = waterBillService.getBillByMonth(userId, billingMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "미납 수도 청구서 조회", description = "사용자의 미납 수도 청구서를 조회합니다.")
    @GetMapping("/users/{userId}/unpaid")
    public ResponseEntity<ApiResponse<List<WaterBillResponse>>> getUnpaidBills(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        List<WaterBillResponse> response = waterBillService.getUnpaidBills(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "수도 청구서 납부 처리", description = "수도 청구서를 납부 완료로 표시합니다.")
    @PatchMapping("/{billId}/pay")
    public ResponseEntity<ApiResponse<WaterBillResponse>> markBillAsPaid(
            @Parameter(description = "청구서 ID") @PathVariable Long billId) {
        WaterBillResponse response = waterBillService.markBillAsPaid(billId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전월 대비 수도비 증감률 조회", description = "전월 대비 수도비 증감률을 퍼센트로 조회합니다.")
    @GetMapping("/users/{userId}/compare")
    public ResponseEntity<ApiResponse<BigDecimal>> compareWithPreviousMonth(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "비교 기준 월 (현재 월)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentMonth) {
        BigDecimal changeRate = waterBillService.compareWithPreviousMonth(userId, currentMonth);
        return ResponseEntity.ok(ApiResponse.success(changeRate));
    }
}
