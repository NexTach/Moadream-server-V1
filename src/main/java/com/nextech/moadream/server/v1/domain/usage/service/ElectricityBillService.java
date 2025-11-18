package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.ElectricityBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.ElectricityBillResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.ElectricityBill;
import com.nextech.moadream.server.v1.domain.usage.repository.ElectricityBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElectricityBillService {

    private final ElectricityBillRepository electricityBillRepository;
    private final UserRepository userRepository;

    @Transactional
    public ElectricityBillResponse createBill(Long userId, ElectricityBillRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BigDecimal totalCharge = request.getBasicCharge().add(request.getEnergyCharge())
                .add(request.getClimateEnvironmentCharge()).add(request.getFuelAdjustmentCharge()).add(request.getVat())
                .add(request.getElectricIndustryFund());

        ElectricityBill bill = ElectricityBill.builder().user(user).billingMonth(request.getBillingMonth())
                .basicCharge(request.getBasicCharge()).energyCharge(request.getEnergyCharge())
                .climateEnvironmentCharge(request.getClimateEnvironmentCharge())
                .fuelAdjustmentCharge(request.getFuelAdjustmentCharge()).vat(request.getVat())
                .electricIndustryFund(request.getElectricIndustryFund()).totalCharge(totalCharge)
                .totalUsage(request.getTotalUsage()).dueDate(request.getDueDate()).isPaid(false).build();

        ElectricityBill savedBill = electricityBillRepository.save(bill);
        return ElectricityBillResponse.from(savedBill);
    }

    public List<ElectricityBillResponse> getUserBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return electricityBillRepository.findByUserOrderByBillingMonthDesc(user).stream()
                .map(ElectricityBillResponse::from).collect(Collectors.toList());
    }

    public ElectricityBillResponse getBillByMonth(Long userId, LocalDate billingMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ElectricityBill bill = electricityBillRepository.findByUserAndBillingMonth(user, billingMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        return ElectricityBillResponse.from(bill);
    }

    public List<ElectricityBillResponse> getUnpaidBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return electricityBillRepository.findByUserAndIsPaid(user, false).stream().map(ElectricityBillResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ElectricityBillResponse markBillAsPaid(Long billId) {
        ElectricityBill bill = electricityBillRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        bill.markAsPaid();
        return ElectricityBillResponse.from(bill);
    }

    public BigDecimal compareWithPreviousMonth(Long userId, LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ElectricityBill currentBill = electricityBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        LocalDate previousMonth = currentMonth.minusMonths(1);
        ElectricityBill previousBill = electricityBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        BigDecimal chargeChange = currentBill.getTotalCharge().subtract(previousBill.getTotalCharge());
        BigDecimal changeRate = previousBill.getTotalCharge().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : chargeChange.divide(previousBill.getTotalCharge(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return changeRate;
    }
}
