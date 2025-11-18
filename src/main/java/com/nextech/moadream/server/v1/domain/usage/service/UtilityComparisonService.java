package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.UtilityComparisonResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.ElectricityBill;
import com.nextech.moadream.server.v1.domain.usage.entity.GasBill;
import com.nextech.moadream.server.v1.domain.usage.entity.WaterBill;
import com.nextech.moadream.server.v1.domain.usage.repository.ElectricityBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.GasBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.WaterBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilityComparisonService {

    private final ElectricityBillRepository electricityBillRepository;
    private final WaterBillRepository waterBillRepository;
    private final GasBillRepository gasBillRepository;
    private final UserRepository userRepository;

    public UtilityComparisonResponse compareAllUtilities(Long userId, LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDate previousMonth = currentMonth.minusMonths(1);

        // 전기 청구서 조회
        ElectricityBill currentElectricity = electricityBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        ElectricityBill previousElectricity = electricityBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        // 수도 청구서 조회
        WaterBill currentWater = waterBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        WaterBill previousWater = waterBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        // 가스 청구서 조회
        GasBill currentGas = gasBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        GasBill previousGas = gasBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        // 각각의 증감률 계산
        BigDecimal electricityChangeRate = calculateChangeRate(currentElectricity.getTotalCharge(),
                previousElectricity.getTotalCharge());

        BigDecimal waterChangeRate = calculateChangeRate(currentWater.getTotalCharge(), previousWater.getTotalCharge());

        BigDecimal gasChangeRate = calculateChangeRate(currentGas.getTotalCharge(), previousGas.getTotalCharge());

        // 총 요금 계산
        BigDecimal totalCurrentCharge = currentElectricity.getTotalCharge().add(currentWater.getTotalCharge())
                .add(currentGas.getTotalCharge());

        BigDecimal totalPreviousCharge = previousElectricity.getTotalCharge().add(previousWater.getTotalCharge())
                .add(previousGas.getTotalCharge());

        BigDecimal totalChangeRate = calculateChangeRate(totalCurrentCharge, totalPreviousCharge);

        return UtilityComparisonResponse.of(currentMonth, electricityChangeRate, waterChangeRate, gasChangeRate,
                currentElectricity.getTotalCharge(), currentWater.getTotalCharge(), currentGas.getTotalCharge(),
                previousElectricity.getTotalCharge(), previousWater.getTotalCharge(), previousGas.getTotalCharge(),
                totalCurrentCharge, totalPreviousCharge, totalChangeRate);
    }

    private BigDecimal calculateChangeRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal change = current.subtract(previous);
        return change.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,
                RoundingMode.HALF_UP);
    }
}
