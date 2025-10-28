package com.nextech.moadream.server.v1.global.scheduler;

import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillScheduler {

    private final UserRepository userRepository;
    private final UsageDataRepository usageDataRepository;
    private final MonthlyBillRepository monthlyBillRepository;

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void generateMonthlyBills() {
        log.info("Starting monthly bill generation...");

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                generateBillsForUser(user, lastMonth, startOfLastMonth, endOfLastMonth);
            } catch (Exception e) {
                log.error("Failed to generate bill for user {}: {}", user.getUserId(), e.getMessage());
            }
        }

        log.info("Monthly bill generation completed.");
    }

    private void generateBillsForUser(User user, YearMonth billingMonth,
                                     LocalDateTime startDate, LocalDateTime endDate) {
        List<UsageData> usageDataList = usageDataRepository
                .findByUserAndMeasuredAtBetween(user, startDate, endDate);

        if (usageDataList.isEmpty()) {
            log.info("No usage data for user {} in {}", user.getUserId(), billingMonth);
            return;
        }

        Map<UtilityType, List<UsageData>> groupedData = usageDataList.stream()
                .collect(Collectors.groupingBy(UsageData::getUtilityType));

        for (Map.Entry<UtilityType, List<UsageData>> entry : groupedData.entrySet()) {
            UtilityType utilityType = entry.getKey();
            List<UsageData> dataList = entry.getValue();

            BigDecimal totalUsage = dataList.stream()
                    .map(UsageData::getUsageAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCharge = dataList.stream()
                    .filter(data -> data.getCurrentCharge() != null)
                    .map(UsageData::getCurrentCharge)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            YearMonth twoMonthsAgo = billingMonth.minusMonths(1);
            BigDecimal previousMonthUsage = getPreviousMonthUsage(user, utilityType, twoMonthsAgo);
            BigDecimal previousMonthCharge = getPreviousMonthCharge(user, utilityType, twoMonthsAgo);

            LocalDate dueDate = billingMonth.plusMonths(1).atDay(15);

            MonthlyBill bill = MonthlyBill.builder()
                    .user(user)
                    .utilityType(utilityType)
                    .billingMonth(billingMonth.atDay(1))
                    .totalUsage(totalUsage)
                    .totalCharge(totalCharge)
                    .previousMonthUsage(previousMonthUsage)
                    .previousMonthCharge(previousMonthCharge)
                    .dueDate(dueDate)
                    .isPaid(false)
                    .build();

            monthlyBillRepository.save(bill);
            log.info("Created bill for user {} - {} - {}",
                    user.getUserId(), utilityType, billingMonth);
        }
    }

    private BigDecimal getPreviousMonthUsage(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> dataList = usageDataRepository
                .findByUserAndMeasuredAtBetween(user, start, end);

        return dataList.stream()
                .filter(data -> data.getUtilityType() == utilityType)
                .map(UsageData::getUsageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPreviousMonthCharge(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> dataList = usageDataRepository
                .findByUserAndMeasuredAtBetween(user, start, end);

        return dataList.stream()
                .filter(data -> data.getUtilityType() == utilityType && data.getCurrentCharge() != null)
                .map(UsageData::getCurrentCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}