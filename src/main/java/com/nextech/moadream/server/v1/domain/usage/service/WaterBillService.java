package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.WaterBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.WaterBillResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.WaterBill;
import com.nextech.moadream.server.v1.domain.usage.repository.WaterBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaterBillService {

    private final WaterBillRepository waterBillRepository;
    private final UserRepository userRepository;

    @Transactional
    public WaterBillResponse createBill(Long userId, WaterBillRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BigDecimal totalCharge = request.getBasicCharge().add(request.getWaterSupplyCharge())
                .add(request.getSewageCharge()).add(request.getWaterUsageCharge());

        WaterBill bill = WaterBill.builder().user(user).billingMonth(request.getBillingMonth())
                .basicCharge(request.getBasicCharge()).waterSupplyCharge(request.getWaterSupplyCharge())
                .sewageCharge(request.getSewageCharge()).waterUsageCharge(request.getWaterUsageCharge())
                .totalCharge(totalCharge).totalUsage(request.getTotalUsage()).dueDate(request.getDueDate())
                .isPaid(false).build();

        WaterBill savedBill = waterBillRepository.save(bill);
        return WaterBillResponse.from(savedBill);
    }

    public List<WaterBillResponse> getUserBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return waterBillRepository.findByUserOrderByBillingMonthDesc(user).stream().map(WaterBillResponse::from)
                .collect(Collectors.toList());
    }

    public WaterBillResponse getBillByMonth(Long userId, LocalDate billingMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        WaterBill bill = waterBillRepository.findByUserAndBillingMonth(user, billingMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        return WaterBillResponse.from(bill);
    }

    public List<WaterBillResponse> getUnpaidBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return waterBillRepository.findByUserAndIsPaid(user, false).stream().map(WaterBillResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public WaterBillResponse markBillAsPaid(Long billId) {
        WaterBill bill = waterBillRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        bill.markAsPaid();
        return WaterBillResponse.from(bill);
    }

    public BigDecimal compareWithPreviousMonth(Long userId, LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        WaterBill currentBill = waterBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        LocalDate previousMonth = currentMonth.minusMonths(1);
        WaterBill previousBill = waterBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        BigDecimal chargeChange = currentBill.getTotalCharge().subtract(previousBill.getTotalCharge());
        BigDecimal changeRate = previousBill.getTotalCharge().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : chargeChange.divide(previousBill.getTotalCharge(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return changeRate;
    }
}
