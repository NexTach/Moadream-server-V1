package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.AllUtilitiesComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillResponse;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillStatisticsResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyBillService {

    private final MonthlyBillRepository monthlyBillRepository;
    private final UserRepository userRepository;

    @Transactional
    public MonthlyBillResponse createBill(Long userId, MonthlyBillRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MonthlyBill bill = MonthlyBill.builder().user(user).utilityType(request.getUtilityType())
                .billingMonth(request.getBillingMonth()).totalUsage(request.getTotalUsage())
                .totalCharge(request.getTotalCharge()).previousMonthUsage(request.getPreviousMonthUsage())
                .previousMonthCharge(request.getPreviousMonthCharge()).dueDate(request.getDueDate()).isPaid(false)
                .build();
        MonthlyBill savedBill = monthlyBillRepository.save(bill);
        return MonthlyBillResponse.from(savedBill);
    }

    public List<MonthlyBillResponse> getUserBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return monthlyBillRepository.findByUser(user).stream().map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
    }

    public List<MonthlyBillResponse> getUserBillsByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return monthlyBillRepository.findByUserAndUtilityType(user, utilityType).stream().map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
    }

    public MonthlyBillResponse getBillByMonth(Long userId, UtilityType utilityType, LocalDate billingMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MonthlyBill bill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, billingMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        return MonthlyBillResponse.from(bill);
    }

    public List<MonthlyBillResponse> getUnpaidBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return monthlyBillRepository.findByUserAndIsPaid(user, false).stream().map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MonthlyBillResponse markBillAsPaid(Long billId) {
        MonthlyBill bill = monthlyBillRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        bill.markAsPaid();
        return MonthlyBillResponse.from(bill);
    }

    public MonthlyBillStatisticsResponse getBillStatistics(Long userId, LocalDate startMonth, LocalDate endMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<MonthlyBill> bills = monthlyBillRepository.findByUserAndBillingMonthBetween(user, startMonth, endMonth);
        if (bills.isEmpty()) {
            return MonthlyBillStatisticsResponse.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    0L, List.of());
        }
        BigDecimal totalCharge = bills.stream().map(MonthlyBill::getTotalCharge).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal averageCharge = totalCharge.divide(BigDecimal.valueOf(bills.size()), 2, RoundingMode.HALF_UP);
        BigDecimal maxCharge = bills.stream().map(MonthlyBill::getTotalCharge).max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal minCharge = bills.stream().map(MonthlyBill::getTotalCharge).min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        Long unpaidCount = bills.stream().filter(bill -> !bill.getIsPaid()).count();
        List<MonthlyBillResponse> billResponses = bills.stream().map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
        return MonthlyBillStatisticsResponse.of(totalCharge, averageCharge, maxCharge, minCharge, unpaidCount,
                billResponses);
    }

    /**
     * 특정 유형의 전월 대비 청구서 비교
     */
    public MonthlyBillComparisonResponse compareWithPreviousMonth(Long userId, UtilityType utilityType,
            LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 월 청구서
        MonthlyBill currentBill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        // 이전 월 청구서
        LocalDate previousMonth = currentMonth.minusMonths(1);
        MonthlyBill previousBill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        return calculateComparison(utilityType, currentMonth, previousMonth, currentBill, previousBill);
    }

    /**
     * 전체 유형의 전월 대비 청구서 비교
     */
    public AllUtilitiesComparisonResponse compareAllUtilitiesWithPreviousMonth(Long userId, LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDate previousMonth = currentMonth.minusMonths(1);

        // 모든 유형에 대해 비교
        List<MonthlyBillComparisonResponse> comparisons = List.of(UtilityType.ELECTRICITY, UtilityType.WATER,
                UtilityType.GAS, UtilityType.INTERNET, UtilityType.MOBILE).stream().map(utilityType -> {
                    try {
                        MonthlyBill currentBill = monthlyBillRepository
                                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, currentMonth).orElse(null);
                        MonthlyBill previousBill = monthlyBillRepository
                                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, previousMonth)
                                .orElse(null);

                        if (currentBill != null && previousBill != null) {
                            return calculateComparison(utilityType, currentMonth, previousMonth, currentBill,
                                    previousBill);
                        }
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(comparison -> comparison != null).collect(Collectors.toList());

        // 전체 합계 계산
        BigDecimal totalCurrentCharge = comparisons.stream().map(MonthlyBillComparisonResponse::getCurrentCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPreviousCharge = comparisons.stream().map(MonthlyBillComparisonResponse::getPreviousCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalChargeChange = totalCurrentCharge.subtract(totalPreviousCharge);

        BigDecimal totalChargeChangeRate = totalPreviousCharge.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : totalChargeChange.divide(totalPreviousCharge, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return AllUtilitiesComparisonResponse.of(currentMonth, comparisons, totalCurrentCharge, totalPreviousCharge,
                totalChargeChangeRate, totalChargeChange);
    }

    /**
     * 증감률 계산 헬퍼 메서드
     */
    private MonthlyBillComparisonResponse calculateComparison(UtilityType utilityType, LocalDate currentMonth,
            LocalDate previousMonth, MonthlyBill currentBill, MonthlyBill previousBill) {

        BigDecimal currentUsage = currentBill.getTotalUsage();
        BigDecimal previousUsage = previousBill.getTotalUsage();
        BigDecimal currentCharge = currentBill.getTotalCharge();
        BigDecimal previousCharge = previousBill.getTotalCharge();

        // 사용량 증감
        BigDecimal usageChange = currentUsage.subtract(previousUsage);
        BigDecimal usageChangeRate = previousUsage.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : usageChange.divide(previousUsage, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        // 요금 증감
        BigDecimal chargeChange = currentCharge.subtract(previousCharge);
        BigDecimal chargeChangeRate = previousCharge.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : chargeChange.divide(previousCharge, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        // 증가 여부
        Boolean isIncrease = chargeChange.compareTo(BigDecimal.ZERO) > 0;

        return MonthlyBillComparisonResponse.of(utilityType, currentMonth, previousMonth, currentUsage, previousUsage,
                currentCharge, previousCharge, usageChangeRate, chargeChangeRate, usageChange, chargeChange,
                isIncrease);
    }
}
