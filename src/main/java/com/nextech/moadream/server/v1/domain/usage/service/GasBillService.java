package com.nextech.moadream.server.v1.domain.usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.dto.GasBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.GasBillResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.GasBill;
import com.nextech.moadream.server.v1.domain.usage.repository.GasBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GasBillService {

    private final GasBillRepository gasBillRepository;
    private final UserRepository userRepository;

    @Transactional
    public GasBillResponse createBill(Long userId, GasBillRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BigDecimal totalCharge = request.getBasicCharge().add(request.getCookingCharge())
                .add(request.getHeatingCharge()).add(request.getSupplyPrice()).add(request.getVat());

        GasBill bill = GasBill.builder().user(user).billingMonth(request.getBillingMonth())
                .basicCharge(request.getBasicCharge()).cookingCharge(request.getCookingCharge())
                .heatingCharge(request.getHeatingCharge()).supplyPrice(request.getSupplyPrice()).vat(request.getVat())
                .totalCharge(totalCharge).totalUsage(request.getTotalUsage()).dueDate(request.getDueDate())
                .isPaid(false).build();

        GasBill savedBill = gasBillRepository.save(bill);
        return GasBillResponse.from(savedBill);
    }

    public List<GasBillResponse> getUserBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return gasBillRepository.findByUserOrderByBillingMonthDesc(user).stream().map(GasBillResponse::from)
                .collect(Collectors.toList());
    }

    public GasBillResponse getBillByMonth(Long userId, LocalDate billingMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        GasBill bill = gasBillRepository.findByUserAndBillingMonth(user, billingMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        return GasBillResponse.from(bill);
    }

    public List<GasBillResponse> getUnpaidBills(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return gasBillRepository.findByUserAndIsPaid(user, false).stream().map(GasBillResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public GasBillResponse markBillAsPaid(Long billId) {
        GasBill bill = gasBillRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        bill.markAsPaid();
        return GasBillResponse.from(bill);
    }

    public BigDecimal compareWithPreviousMonth(Long userId, LocalDate currentMonth) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GasBill currentBill = gasBillRepository.findByUserAndBillingMonth(user, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        LocalDate previousMonth = currentMonth.minusMonths(1);
        GasBill previousBill = gasBillRepository.findByUserAndBillingMonth(user, previousMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));

        BigDecimal chargeChange = currentBill.getTotalCharge().subtract(previousBill.getTotalCharge());
        BigDecimal changeRate = previousBill.getTotalCharge().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : chargeChange.divide(previousBill.getTotalCharge(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return changeRate;
    }
}
