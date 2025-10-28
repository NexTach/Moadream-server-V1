package com.nextech.moadream.server.v1.domain.usage.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyBillService {

    private final MonthlyBillRepository monthlyBillRepository;
    private final UserRepository userRepository;

    @Transactional
    public MonthlyBillResponse createBill(Long userId, MonthlyBillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MonthlyBill bill = MonthlyBill.builder()
                .user(user)
                .utilityType(request.getUtilityType())
                .billingMonth(request.getBillingMonth())
                .totalUsage(request.getTotalUsage())
                .totalCharge(request.getTotalCharge())
                .previousMonthUsage(request.getPreviousMonthUsage())
                .previousMonthCharge(request.getPreviousMonthCharge())
                .dueDate(request.getDueDate())
                .isPaid(false)
                .build();

        MonthlyBill savedBill = monthlyBillRepository.save(bill);
        return MonthlyBillResponse.from(savedBill);
    }

    public List<MonthlyBillResponse> getUserBills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return monthlyBillRepository.findByUser(user).stream()
                .map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
    }

    public List<MonthlyBillResponse> getUserBillsByType(Long userId, UtilityType utilityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return monthlyBillRepository.findByUserAndUtilityType(user, utilityType).stream()
                .map(MonthlyBillResponse::from)
                .collect(Collectors.toList());
    }

    public MonthlyBillResponse getBillByMonth(Long userId, UtilityType utilityType, LocalDate billingMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MonthlyBill bill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, utilityType, billingMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        return MonthlyBillResponse.from(bill);
    }

    public List<MonthlyBillResponse> getUnpaidBills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return monthlyBillRepository.findByUserAndIsPaid(user, false).stream()
                .map(MonthlyBillResponse::from)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<MonthlyBill> bills = monthlyBillRepository
                .findByUserAndBillingMonthBetween(user, startMonth, endMonth);

        if (bills.isEmpty()) {
            return MonthlyBillStatisticsResponse.of(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, List.of());
        }

        BigDecimal totalCharge = bills.stream()
                .map(MonthlyBill::getTotalCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageCharge = totalCharge
                .divide(BigDecimal.valueOf(bills.size()), 2, RoundingMode.HALF_UP);

        BigDecimal maxCharge = bills.stream()
                .map(MonthlyBill::getTotalCharge)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minCharge = bills.stream()
                .map(MonthlyBill::getTotalCharge)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        Long unpaidCount = bills.stream()
                .filter(bill -> !bill.getIsPaid())
                .count();

        List<MonthlyBillResponse> billResponses = bills.stream()
                .map(MonthlyBillResponse::from)
                .collect(Collectors.toList());

        return MonthlyBillStatisticsResponse.of(
                totalCharge, averageCharge, maxCharge, minCharge, unpaidCount, billResponses);
    }
}