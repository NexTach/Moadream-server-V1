package com.nextech.moadream.server.v1.global.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillScheduler {

    private final UserRepository userRepository;
    private final UsageDataRepository usageDataRepository;
    private final MonthlyBillRepository monthlyBillRepository;
    private final UsageAlertRepository usageAlertRepository;
    private final UserSettingRepository userSettingRepository;

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
                generateMonthlyAlerts(user, lastMonth);
            } catch (Exception e) {
                log.error("Failed to generate bill for user {}: {}", user.getUserId(), e.getMessage());
            }
        }
        log.info("Monthly bill generation completed.");
    }

    private void generateBillsForUser(User user, YearMonth billingMonth, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<UsageData> usageDataList = usageDataRepository.findByUserAndMeasuredAtBetween(user, startDate, endDate);
        if (usageDataList.isEmpty()) {
            log.info("No usage data for user {} in {}", user.getUserId(), billingMonth);
            return;
        }
        Map<UtilityType, List<UsageData>> groupedData = usageDataList.stream()
                .collect(Collectors.groupingBy(UsageData::getUtilityType));
        for (Map.Entry<UtilityType, List<UsageData>> entry : groupedData.entrySet()) {
            UtilityType utilityType = entry.getKey();
            List<UsageData> dataList = entry.getValue();
            BigDecimal totalUsage = dataList.stream().map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            BigDecimal totalCharge = dataList.stream().map(UsageData::getCurrentCharge).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            YearMonth twoMonthsAgo = billingMonth.minusMonths(1);
            BigDecimal previousMonthUsage = getPreviousMonthUsage(user, utilityType, twoMonthsAgo);
            BigDecimal previousMonthCharge = getPreviousMonthCharge(user, utilityType, twoMonthsAgo);
            LocalDate dueDate = billingMonth.plusMonths(1).atDay(15);
            MonthlyBill bill = MonthlyBill.builder().user(user).utilityType(utilityType)
                    .billingMonth(billingMonth.atDay(1)).totalUsage(totalUsage).totalCharge(totalCharge)
                    .previousMonthUsage(previousMonthUsage).previousMonthCharge(previousMonthCharge).dueDate(dueDate)
                    .isPaid(false).build();
            monthlyBillRepository.save(bill);
            log.info("Created bill for user {} - {} - {}", user.getUserId(), utilityType, billingMonth);
        }
    }

    private BigDecimal getPreviousMonthUsage(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<UsageData> dataList = usageDataRepository.findByUserAndMeasuredAtBetween(user, start, end);
        return dataList.stream().filter(data -> data.getUtilityType() == utilityType).map(UsageData::getUsageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPreviousMonthCharge(User user, UtilityType utilityType, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<UsageData> dataList = usageDataRepository.findByUserAndMeasuredAtBetween(user, start, end);
        return dataList.stream().filter(data -> data.getUtilityType() == utilityType && data.getCurrentCharge() != null)
                .map(UsageData::getCurrentCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void generateMonthlyAlerts(User user, YearMonth billingMonth) {
        UserSetting userSetting = userSettingRepository.findByUser(user).orElse(null);
        if (userSetting == null || userSetting.getMonthlyBudget() == null) {
            return;
        }

        LocalDateTime startOfMonth = billingMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = billingMonth.atEndOfMonth().atTime(23, 59, 59);
        List<UsageData> monthlyUsageData = usageDataRepository.findByUserAndMeasuredAtBetween(user, startOfMonth,
                endOfMonth);

        Map<UtilityType, List<UsageData>> groupedData = monthlyUsageData.stream()
                .collect(Collectors.groupingBy(UsageData::getUtilityType));

        for (Map.Entry<UtilityType, List<UsageData>> entry : groupedData.entrySet()) {
            UtilityType utilityType = entry.getKey();
            List<UsageData> dataList = entry.getValue();

            BigDecimal totalCharge = dataList.stream().map(UsageData::getCurrentCharge).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalUsage = dataList.stream().map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO,
                    BigDecimal::add);

            // 월 마감 후 긍정적 피드백 알람 생성 (예산 대비 90% 이하 사용)
            if (totalCharge.compareTo(userSetting.getMonthlyBudget().multiply(BigDecimal.valueOf(0.9))) <= 0) {
                BigDecimal savingsAmount = userSetting.getMonthlyBudget().subtract(totalCharge);
                String alertMessage = String.format("월 마감! %s을(를) 예산 내에서 효율적으로 사용하셨습니다. %.0f원을 절약하셨어요!",
                        getUtilityTypeKoreanName(utilityType), savingsAmount);
                UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType)
                        .alertType(AlertType.POSITIVE_FEEDBACK).alertMessage(alertMessage).isRead(false).build();
                usageAlertRepository.save(alert);
                log.info("Created positive feedback alert for user {} - {}", user.getUserId(), utilityType);
            }

            // 전월 대비 감소 시 긍정적 피드백 알람
            YearMonth previousMonth = billingMonth.minusMonths(1);
            BigDecimal previousMonthCharge = getPreviousMonthCharge(user, utilityType, previousMonth);
            if (previousMonthCharge.compareTo(BigDecimal.ZERO) > 0 && totalCharge.compareTo(previousMonthCharge) < 0) {
                BigDecimal reductionPercentage = previousMonthCharge.subtract(totalCharge)
                        .divide(previousMonthCharge, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                if (reductionPercentage.compareTo(BigDecimal.valueOf(10)) >= 0) {
                    String alertMessage = String.format("%s 사용량이 전월 대비 %.1f%% 감소했습니다. 훌륭해요!",
                            getUtilityTypeKoreanName(utilityType), reductionPercentage);
                    UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType)
                            .alertType(AlertType.POSITIVE_FEEDBACK).alertMessage(alertMessage).isRead(false).build();
                    usageAlertRepository.save(alert);
                    log.info("Created usage reduction alert for user {} - {}", user.getUserId(), utilityType);
                }
            }
        }
    }

    private String getUtilityTypeKoreanName(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "전기";
            case WATER -> "수도";
            case GAS -> "가스";
        };
    }
}
