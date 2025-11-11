package com.nextech.moadream.server.v1.domain.chat.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UsageDataRepository usageDataRepository;
    private final MonthlyBillRepository monthlyBillRepository;
    private final UserSettingRepository userSettingRepository;
    private final RecommendationRepository recommendationRepository;

    public String buildUserContext(User user) {
        StringBuilder context = new StringBuilder();

        context.append("=== 사용자 정보 ===\n");
        context.append("이름: ").append(user.getName()).append("\n");
        context.append("주소: ").append(user.getAddress() != null ? user.getAddress() : "정보 없음").append("\n");
        context.append("연락처: ").append(user.getPhone() != null ? user.getPhone() : "정보 없음").append("\n\n");

        UserSetting userSetting = userSettingRepository.findByUser(user).orElse(null);
        if (userSetting != null) {
            context.append("=== 예산 설정 ===\n");
            context.append("월 예산: ")
                    .append(userSetting.getMonthlyBudget() != null ? userSetting.getMonthlyBudget() + "원" : "미설정")
                    .append("\n");
            context.append("알림 임계값: ")
                    .append(userSetting.getAlertThreshold() != null ? userSetting.getAlertThreshold() + "%" : "미설정")
                    .append("\n\n");
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<UsageData> currentMonthUsage = usageDataRepository.findByUserAndMeasuredAtBetween(user, startOfMonth,
                endOfMonth);

        if (!currentMonthUsage.isEmpty()) {
            context.append("=== 현재 월 사용량 (").append(currentMonth).append(") ===\n");
            Map<UtilityType, List<UsageData>> grouped = currentMonthUsage.stream()
                    .collect(Collectors.groupingBy(UsageData::getUtilityType));

            for (Map.Entry<UtilityType, List<UsageData>> entry : grouped.entrySet()) {
                BigDecimal totalUsage = entry.getValue().stream().map(UsageData::getUsageAmount).reduce(BigDecimal.ZERO,
                        BigDecimal::add);
                BigDecimal totalCharge = entry.getValue().stream().map(UsageData::getCurrentCharge)
                        .filter(c -> c != null).reduce(BigDecimal.ZERO, BigDecimal::add);

                context.append(getUtilityTypeKoreanName(entry.getKey())).append(": 사용량 ").append(totalUsage)
                        .append(getUnit(entry.getKey())).append(", 요금 ").append(totalCharge).append("원\n");
            }
            context.append("\n");
        }

        YearMonth lastMonth = currentMonth.minusMonths(1);
        List<MonthlyBill> recentBills = monthlyBillRepository.findByUserAndBillingMonthBetween(user, lastMonth.atDay(1),
                lastMonth.atDay(1));

        if (!recentBills.isEmpty()) {
            context.append("=== 전월 청구서 (").append(lastMonth).append(") ===\n");
            for (MonthlyBill bill : recentBills) {
                context.append(getUtilityTypeKoreanName(bill.getUtilityType())).append(": ")
                        .append(bill.getTotalCharge()).append("원 (사용량: ").append(bill.getTotalUsage())
                        .append(getUnit(bill.getUtilityType())).append(")\n");
            }
            context.append("\n");
        }

        List<Recommendation> recommendations = recommendationRepository.findByUserAndIsApplied(user, false);
        if (!recommendations.isEmpty()) {
            context.append("=== 최근 AI 추천 ===\n");
            recommendations.stream().limit(3).forEach(rec -> {
                context.append("- ").append(rec.getRecommendationText()).append(" (예상 절감액: ")
                        .append(rec.getExpectedSavings()).append("원)\n");
            });
            context.append("\n");
        }

        return context.toString();
    }

    private String getUtilityTypeKoreanName(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "전기";
            case WATER -> "수도";
            case GAS -> "가스";
        };
    }

    private String getUnit(UtilityType utilityType) {
        return switch (utilityType) {
            case ELECTRICITY -> "kWh";
            case WATER -> "㎥";
            case GAS -> "㎥";
        };
    }
}
