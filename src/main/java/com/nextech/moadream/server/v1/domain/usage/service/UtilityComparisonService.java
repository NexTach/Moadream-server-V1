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

        var currentElectricityOpt = electricityBillRepository.findByUserAndBillingMonth(user, currentMonth);
        var previousElectricityOpt = electricityBillRepository.findByUserAndBillingMonth(user, previousMonth);

        var currentWaterOpt = waterBillRepository.findByUserAndBillingMonth(user, currentMonth);
        var previousWaterOpt = waterBillRepository.findByUserAndBillingMonth(user, previousMonth);

        var currentGasOpt = gasBillRepository.findByUserAndBillingMonth(user, currentMonth);
        var previousGasOpt = gasBillRepository.findByUserAndBillingMonth(user, previousMonth);

        BigDecimal electricityChangeRate = calculateOptionalChangeRate(
                currentElectricityOpt.map(ElectricityBill::getTotalCharge),
                previousElectricityOpt.map(ElectricityBill::getTotalCharge));

        BigDecimal waterChangeRate = calculateOptionalChangeRate(currentWaterOpt.map(WaterBill::getTotalCharge),
                previousWaterOpt.map(WaterBill::getTotalCharge));

        BigDecimal gasChangeRate = calculateOptionalChangeRate(currentGasOpt.map(GasBill::getTotalCharge),
                previousGasOpt.map(GasBill::getTotalCharge));

        BigDecimal totalCurrentCharge = BigDecimal.ZERO;
        totalCurrentCharge = totalCurrentCharge
                .add(currentElectricityOpt.map(ElectricityBill::getTotalCharge).orElse(BigDecimal.ZERO));
        totalCurrentCharge = totalCurrentCharge
                .add(currentWaterOpt.map(WaterBill::getTotalCharge).orElse(BigDecimal.ZERO));
        totalCurrentCharge = totalCurrentCharge.add(currentGasOpt.map(GasBill::getTotalCharge).orElse(BigDecimal.ZERO));

        BigDecimal totalPreviousCharge = BigDecimal.ZERO;
        totalPreviousCharge = totalPreviousCharge
                .add(previousElectricityOpt.map(ElectricityBill::getTotalCharge).orElse(BigDecimal.ZERO));
        totalPreviousCharge = totalPreviousCharge
                .add(previousWaterOpt.map(WaterBill::getTotalCharge).orElse(BigDecimal.ZERO));
        totalPreviousCharge = totalPreviousCharge
                .add(previousGasOpt.map(GasBill::getTotalCharge).orElse(BigDecimal.ZERO));

        BigDecimal totalChangeRate = calculateChangeRate(totalCurrentCharge, totalPreviousCharge);

        return UtilityComparisonResponse.of(currentMonth, electricityChangeRate, waterChangeRate, gasChangeRate,
                currentElectricityOpt.map(ElectricityBill::getTotalCharge).orElse(null),
                currentWaterOpt.map(WaterBill::getTotalCharge).orElse(null),
                currentGasOpt.map(GasBill::getTotalCharge).orElse(null),
                previousElectricityOpt.map(ElectricityBill::getTotalCharge).orElse(null),
                previousWaterOpt.map(WaterBill::getTotalCharge).orElse(null),
                previousGasOpt.map(GasBill::getTotalCharge).orElse(null), totalCurrentCharge, totalPreviousCharge,
                totalChangeRate);
    }

    private BigDecimal calculateOptionalChangeRate(java.util.Optional<BigDecimal> current,
            java.util.Optional<BigDecimal> previous) {
        if (current.isEmpty() || previous.isEmpty()) {
            return null;
        }
        return calculateChangeRate(current.get(), previous.get());
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
