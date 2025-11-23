package com.nextech.moadream.server.v1.global.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
import com.nextech.moadream.server.v1.domain.analysis.entity.SavingsTracking;
import com.nextech.moadream.server.v1.domain.analysis.entity.UsagePattern;
import com.nextech.moadream.server.v1.domain.analysis.enums.FrequencyType;
import com.nextech.moadream.server.v1.domain.analysis.enums.RecommendationType;
import com.nextech.moadream.server.v1.domain.analysis.repository.RecommendationRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.SavingsTrackingRepository;
import com.nextech.moadream.server.v1.domain.analysis.repository.UsagePatternRepository;
import com.nextech.moadream.server.v1.domain.chat.entity.ChatMessage;
import com.nextech.moadream.server.v1.domain.chat.entity.ChatSession;
import com.nextech.moadream.server.v1.domain.chat.enums.MessageRole;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatMessageRepository;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatSessionRepository;
import com.nextech.moadream.server.v1.domain.privacy.entity.PrivacyLog;
import com.nextech.moadream.server.v1.domain.privacy.enums.ActionType;
import com.nextech.moadream.server.v1.domain.privacy.repository.PrivacyLogRepository;
import com.nextech.moadream.server.v1.domain.usage.entity.ElectricityBill;
import com.nextech.moadream.server.v1.domain.usage.entity.GasBill;
import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.entity.UsageData;
import com.nextech.moadream.server.v1.domain.usage.entity.WaterBill;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.usage.repository.ElectricityBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.GasBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageAlertRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.UsageDataRepository;
import com.nextech.moadream.server.v1.domain.usage.repository.WaterBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.entity.UserBill;
import com.nextech.moadream.server.v1.domain.user.entity.UserSetting;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserBillRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.domain.user.repository.UserSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용되지 않음
 */
@Slf4j
// @Component
// @Order(2)
@RequiredArgsConstructor
@Deprecated
public class MockDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserBillRepository userBillRepository;
    private final UsageDataRepository usageDataRepository;
    private final ElectricityBillRepository electricityBillRepository;
    private final GasBillRepository gasBillRepository;
    private final WaterBillRepository waterBillRepository;
    private final MonthlyBillRepository monthlyBillRepository;
    private final UsageAlertRepository usageAlertRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UsagePatternRepository usagePatternRepository;
    private final RecommendationRepository recommendationRepository;
    private final SavingsTrackingRepository savingsTrackingRepository;
    private final PrivacyLogRepository privacyLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Mock data already exists. Skipping data initialization.");
            return;
        }

        log.info("Starting mock data initialization...");

        User user1 = createUser1();
        createUser1Data(user1);

        User user2 = createUser2();
        createUser2Data(user2);

        log.info("Mock data initialization completed successfully!");
    }

    private User createUser1() {
        User user = User.builder().email("loolloo080313@gmail.com").passwordHash(passwordEncoder.encode("12345678!"))
                .name("이주언").phone("010-1234-5678").address("서울특별시 강남구 역삼동 123-45").dateOfBirth("2003-08-13")
                .userVerificationCode("USER001").provider("LOCAL").providerId(null).build();
        return userRepository.save(user);
    }

    private void createUser1Data(User user) {
        // 사용자 설정
        UserSetting setting = UserSetting.builder().user(user).monthlyBudget(new BigDecimal("150000"))
                .alertThreshold(new BigDecimal("80.00")).pushEnabled(true).emailEnabled(true)
                .efficiencyScore(new BigDecimal("72.50")).build();
        userSettingRepository.save(setting);

        // 등록된 고지서 정보
        UserBill elecBill = UserBill.builder().user(user).utilityType(UtilityType.ELECTRICITY).billNumber("1234567890")
                .generationName("역삼동 원룸 101호").isVerified(true).build();
        userBillRepository.save(elecBill);

        UserBill gasBill = UserBill.builder().user(user).utilityType(UtilityType.GAS).billNumber("9876543210")
                .generationName("역삼동 원룸 101호").isVerified(true).build();
        userBillRepository.save(gasBill);

        // 최근 3개월 전기 요금
        createElectricityBill(user, LocalDate.now().minusMonths(2), new BigDecimal("250.00"),
                new BigDecimal("38500.00"), new BigDecimal("45000.00"), new BigDecimal("90.00"), true);
        createElectricityBill(user, LocalDate.now().minusMonths(1), new BigDecimal("220.00"),
                new BigDecimal("32000.00"), new BigDecimal("40000.00"), new BigDecimal("85.00"), true);
        createElectricityBill(user, LocalDate.now(), new BigDecimal("180.00"), new BigDecimal("25000.00"),
                new BigDecimal("32000.00"), new BigDecimal("70.00"), false);

        // 최근 3개월 가스 요금
        createGasBill(user, LocalDate.now().minusMonths(2), new BigDecimal("45.00"), new BigDecimal("35000.00"),
                new BigDecimal("38000.00"), true);
        createGasBill(user, LocalDate.now().minusMonths(1), new BigDecimal("38.00"), new BigDecimal("28000.00"),
                new BigDecimal("31000.00"), true);
        createGasBill(user, LocalDate.now(), new BigDecimal("30.00"), new BigDecimal("22000.00"),
                new BigDecimal("25000.00"), false);

        // 최근 3개월 수도 요금
        createWaterBill(user, LocalDate.now().minusMonths(2), new BigDecimal("8.5"), new BigDecimal("15000.00"), true);
        createWaterBill(user, LocalDate.now().minusMonths(1), new BigDecimal("7.8"), new BigDecimal("14000.00"), true);
        createWaterBill(user, LocalDate.now(), new BigDecimal("7.2"), new BigDecimal("13000.00"), false);

        // 월별 통합 요금
        createMonthlyBill(user, UtilityType.ELECTRICITY, LocalDate.now().minusMonths(1), new BigDecimal("220.00"),
                new BigDecimal("40000.00"), new BigDecimal("250.00"), new BigDecimal("45000.00"), true);
        createMonthlyBill(user, UtilityType.ELECTRICITY, LocalDate.now(), new BigDecimal("180.00"),
                new BigDecimal("32000.00"), new BigDecimal("220.00"), new BigDecimal("40000.00"), false);

        // 실시간 사용량 데이터 (최근 30일)
        for (int i = 30; i >= 0; i--) {
            LocalDateTime measuredAt = LocalDateTime.now().minusDays(i);
            createUsageData(user, UtilityType.ELECTRICITY, new BigDecimal(5.5 + Math.random() * 2), "kWh", measuredAt);
            if (i % 3 == 0) {
                createUsageData(user, UtilityType.GAS, new BigDecimal(0.8 + Math.random() * 0.5), "m³", measuredAt);
            }
            if (i % 7 == 0) {
                createUsageData(user, UtilityType.WATER, new BigDecimal(0.2 + Math.random() * 0.15), "m³", measuredAt);
            }
        }

        // 사용 패턴
        createUsagePattern(user, UtilityType.ELECTRICITY, FrequencyType.DAILY, new BigDecimal("6.00"),
                new BigDecimal("8.50"), new BigDecimal("4.20"), "감소");
        createUsagePattern(user, UtilityType.ELECTRICITY, FrequencyType.MONTHLY, new BigDecimal("180.00"),
                new BigDecimal("250.00"), new BigDecimal("150.00"), "감소");
        createUsagePattern(user, UtilityType.GAS, FrequencyType.MONTHLY, new BigDecimal("30.00"),
                new BigDecimal("45.00"), new BigDecimal("20.00"), "안정");

        // 추천 사항
        Recommendation rec1 = createRecommendation(user, UtilityType.ELECTRICITY, RecommendationType.TIME_SHIFT,
                "심야 시간대(23:00-07:00)에 세탁기, 식기세척기 등 대용량 가전 사용을 권장합니다. 약 15-20%의 전기료 절감 효과가 있습니다.",
                new BigDecimal("8000.00"), "쉬움", true);
        Recommendation rec2 = createRecommendation(user, UtilityType.ELECTRICITY, RecommendationType.BEHAVIOR_CHANGE,
                "사용하지 않는 전자기기의 플러그를 뽑아 대기전력을 줄이세요. 월 평균 3,000-5,000원 절약 가능합니다.", new BigDecimal("4000.00"), "쉬움",
                false);

        // 절감 추적
        createSavingsTracking(user, rec1, UtilityType.ELECTRICITY, LocalDate.now().minusMonths(1),
                new BigDecimal("220.00"), new BigDecimal("40000.00"), new BigDecimal("32000.00"));

        // 알림
        createUsageAlert(user, UtilityType.ELECTRICITY, AlertType.POSITIVE_FEEDBACK,
                "훌륭합니다! 이번 달 전기 사용량이 지난 달 대비 18% 감소했습니다.", true);
        createUsageAlert(user, UtilityType.ELECTRICITY, AlertType.HIGH_USAGE,
                "오늘 전기 사용량이 평소보다 30% 높습니다. 에어컨 사용을 확인해보세요.", false);

        // 채팅 세션
        ChatSession session = createChatSession(user, "전기료 절약 방법 문의");
        createChatMessage(session, MessageRole.USER, "전기료를 줄일 수 있는 방법이 있을까요?");
        createChatMessage(session, MessageRole.ASSISTANT,
                "네, 몇 가지 효과적인 방법을 추천드립니다:\n\n1. 심야 시간대 활용: 23시-07시 사이에 세탁기, 식기세척기 등을 사용하면 약 15-20% 절약됩니다.\n2. 대기전력 차단: 사용하지 않는 전자기기의 플러그를 뽑으면 월 3,000-5,000원 절약 가능합니다.\n3. LED 조명 교체: 기존 백열등을 LED로 교체하면 전력 소비가 80% 감소합니다.\n\n회원님의 현재 사용 패턴을 분석한 결과, 1번 방법을 먼저 실천하시면 월 약 8,000원을 절약하실 수 있습니다.");

        // 개인정보 로그
        createPrivacyLog(user, ActionType.DATA_COLLECTED, "사용량 수집", "전기 사용량 데이터 자동 수집");
        createPrivacyLog(user, ActionType.DATA_PROCESSED, "AI 분석", "사용 패턴 분석 및 추천 생성");
    }

    // ============================================
    // 사용자 2: 이영희 (4인 가구, 아파트 거주)
    // ============================================

    private User createUser2() {
        User user = User.builder().email("test.user2@example.com").passwordHash(passwordEncoder.encode("12345678!"))
                .name("김민수").phone("010-9876-5432").address("서울특별시 송파구 잠실동 123-45").dateOfBirth("2001-11-25")
                .userVerificationCode("USER002").provider("LOCAL").providerId(null).build();
        return userRepository.save(user);
    }

    private void createUser2Data(User user) {
        // 사용자 설정
        UserSetting setting = UserSetting.builder().user(user).monthlyBudget(new BigDecimal("300000"))
                .alertThreshold(new BigDecimal("85.00")).pushEnabled(true).emailEnabled(true)
                .efficiencyScore(new BigDecimal("68.30")).build();
        userSettingRepository.save(setting);

        // 등록된 고지서 정보
        UserBill elecBill = UserBill.builder().user(user).utilityType(UtilityType.ELECTRICITY).billNumber("5551234567")
                .generationName("정자동 아파트 102-1504").isVerified(true).build();
        userBillRepository.save(elecBill);

        UserBill gasBill = UserBill.builder().user(user).utilityType(UtilityType.GAS).billNumber("5559876543")
                .generationName("정자동 아파트 102-1504").isVerified(true).build();
        userBillRepository.save(gasBill);

        UserBill waterBill = UserBill.builder().user(user).utilityType(UtilityType.WATER).billNumber("5555555555")
                .generationName("정자동 아파트 102-1504").isVerified(true).build();
        userBillRepository.save(waterBill);

        // 최근 3개월 전기 요금 (4인 가구이므로 사용량 높음)
        createElectricityBill(user, LocalDate.now().minusMonths(2), new BigDecimal("520.00"),
                new BigDecimal("95000.00"), new BigDecimal("110000.00"), new BigDecimal("200.00"), true);
        createElectricityBill(user, LocalDate.now().minusMonths(1), new BigDecimal("580.00"),
                new BigDecimal("110000.00"), new BigDecimal("128000.00"), new BigDecimal("230.00"), true);
        createElectricityBill(user, LocalDate.now(), new BigDecimal("650.00"), new BigDecimal("125000.00"),
                new BigDecimal("145000.00"), new BigDecimal("260.00"), false);

        // 최근 3개월 가스 요금 (난방 사용으로 높음)
        createGasBill(user, LocalDate.now().minusMonths(2), new BigDecimal("120.00"), new BigDecimal("85000.00"),
                new BigDecimal("95000.00"), true);
        createGasBill(user, LocalDate.now().minusMonths(1), new BigDecimal("135.00"), new BigDecimal("98000.00"),
                new BigDecimal("108000.00"), true);
        createGasBill(user, LocalDate.now(), new BigDecimal("150.00"), new BigDecimal("110000.00"),
                new BigDecimal("122000.00"), false);

        // 최근 3개월 수도 요금 (4인 가구)
        createWaterBill(user, LocalDate.now().minusMonths(2), new BigDecimal("25.5"), new BigDecimal("42000.00"), true);
        createWaterBill(user, LocalDate.now().minusMonths(1), new BigDecimal("27.3"), new BigDecimal("45000.00"), true);
        createWaterBill(user, LocalDate.now(), new BigDecimal("28.8"), new BigDecimal("48000.00"), false);

        // 월별 통합 요금
        createMonthlyBill(user, UtilityType.ELECTRICITY, LocalDate.now().minusMonths(1), new BigDecimal("580.00"),
                new BigDecimal("128000.00"), new BigDecimal("520.00"), new BigDecimal("110000.00"), true);
        createMonthlyBill(user, UtilityType.ELECTRICITY, LocalDate.now(), new BigDecimal("650.00"),
                new BigDecimal("145000.00"), new BigDecimal("580.00"), new BigDecimal("128000.00"), false);
        createMonthlyBill(user, UtilityType.GAS, LocalDate.now().minusMonths(1), new BigDecimal("135.00"),
                new BigDecimal("108000.00"), new BigDecimal("120.00"), new BigDecimal("95000.00"), true);
        createMonthlyBill(user, UtilityType.GAS, LocalDate.now(), new BigDecimal("150.00"), new BigDecimal("122000.00"),
                new BigDecimal("135.00"), new BigDecimal("108000.00"), false);

        // 실시간 사용량 데이터 (최근 30일)
        for (int i = 30; i >= 0; i--) {
            LocalDateTime measuredAt = LocalDateTime.now().minusDays(i);
            createUsageData(user, UtilityType.ELECTRICITY, new BigDecimal(18.0 + Math.random() * 5), "kWh", measuredAt);
            if (i % 2 == 0) {
                createUsageData(user, UtilityType.GAS, new BigDecimal(4.5 + Math.random() * 1.5), "m³", measuredAt);
            }
            if (i % 5 == 0) {
                createUsageData(user, UtilityType.WATER, new BigDecimal(0.8 + Math.random() * 0.3), "m³", measuredAt);
            }
        }

        // 사용 패턴
        createUsagePattern(user, UtilityType.ELECTRICITY, FrequencyType.DAILY, new BigDecimal("21.00"),
                new BigDecimal("28.00"), new BigDecimal("15.00"), "증가");
        createUsagePattern(user, UtilityType.ELECTRICITY, FrequencyType.MONTHLY, new BigDecimal("650.00"),
                new BigDecimal("750.00"), new BigDecimal("520.00"), "증가");
        createUsagePattern(user, UtilityType.GAS, FrequencyType.MONTHLY, new BigDecimal("150.00"),
                new BigDecimal("180.00"), new BigDecimal("120.00"), "증가");
        createUsagePattern(user, UtilityType.WATER, FrequencyType.MONTHLY, new BigDecimal("28.00"),
                new BigDecimal("32.00"), new BigDecimal("24.00"), "안정");

        // 추천 사항
        Recommendation rec1 = createRecommendation(user, UtilityType.ELECTRICITY, RecommendationType.APPLIANCE_UPGRADE,
                "10년 이상 된 냉장고를 에너지 효율 1등급 제품으로 교체하면 연간 약 150,000원을 절약할 수 있습니다.", new BigDecimal("12500.00"), "보통",
                false);
        Recommendation rec2 = createRecommendation(user, UtilityType.GAS, RecommendationType.USAGE_REDUCTION,
                "난방 온도를 1도만 낮춰도 가스 사용량이 약 7% 감소합니다. 20도를 권장합니다.", new BigDecimal("8500.00"), "쉬움", true);
        Recommendation rec3 = createRecommendation(user, UtilityType.WATER, RecommendationType.BEHAVIOR_CHANGE,
                "샤워 시간을 5분만 줄여도 월 3,000-5,000원 절약 가능합니다. 절수형 샤워기 사용을 권장합니다.", new BigDecimal("4000.00"), "쉬움", false);

        // 절감 추적
        createSavingsTracking(user, rec2, UtilityType.GAS, LocalDate.now().minusMonths(1), new BigDecimal("135.00"),
                new BigDecimal("108000.00"), new BigDecimal("100000.00"));

        // 알림
        createUsageAlert(user, UtilityType.ELECTRICITY, AlertType.BUDGET_EXCEEDED,
                "이번 달 전기 요금이 예산의 85%를 초과했습니다. 현재 145,000원으로 예산 대비 48% 초과 중입니다.", false);
        createUsageAlert(user, UtilityType.GAS, AlertType.HIGH_USAGE, "가스 사용량이 지난 달보다 11% 증가했습니다. 난방 온도 설정을 확인해보세요.",
                true);
        createUsageAlert(user, UtilityType.WATER, AlertType.UNUSUAL_PATTERN,
                "최근 3일간 수도 사용량이 평소보다 40% 높습니다. 누수를 확인해보세요.", false);

        // 채팅 세션
        ChatSession session1 = createChatSession(user, "전기료 급증 원인 문의");
        createChatMessage(session1, MessageRole.USER, "이번 달 전기료가 갑자기 많이 올랐는데 왜 그런가요?");
        createChatMessage(session1, MessageRole.ASSISTANT,
                "분석 결과, 다음과 같은 원인이 있습니다:\n\n1. 사용량 증가: 지난 달 580kWh → 이번 달 650kWh (12% 증가)\n2. 누진세 구간 상승: 500kWh 초과로 3단계 누진세 적용\n3. 계절 요인: 겨울철 전기 난방기 사용 증가\n\n절약 방법:\n- 난방 온도 1-2도 낮추기\n- 사용하지 않는 방의 난방 끄기\n- 전기장판 대신 이불 활용\n\n이렇게 하시면 다음 달 약 20,000-30,000원 절약 가능합니다.");
        createChatMessage(session1, MessageRole.USER, "난방 온도를 몇 도로 설정하는 게 좋을까요?");
        createChatMessage(session1, MessageRole.ASSISTANT,
                "WHO 권장 실내 온도는 18-20도입니다.\n\n현재 회원님의 패턴을 보면:\n- 거실: 22도 → 20도로 조정 권장\n- 침실: 20도 → 18도로 조정 권장 (수면 시)\n\n온도를 2도만 낮춰도 월 15,000-20,000원 절약되며, 건강에도 더 좋습니다. 추우시면 내복이나 담요를 활용해보세요!");

        ChatSession session2 = createChatSession(user, "가스 요금 절약 팁");
        createChatMessage(session2, MessageRole.USER, "가스 요금을 줄이려면 어떻게 해야 하나요?");
        createChatMessage(session2, MessageRole.ASSISTANT,
                "가스 요금 절약 팁입니다:\n\n**난방 관련**\n1. 난방 온도 1도만 낮춰도 7% 절약 (월 약 8,500원)\n2. 외출 시 온도 자동 조절 모드 활용\n3. 창문 틈새 단열 보강\n\n**취사 관련**\n1. 압력솥 활용 (일반 냄비 대비 30% 절약)\n2. 뚜껑 덮고 조리\n3. 예열 시간 최소화\n\n**온수 관련**\n1. 샤워 시간 줄이기 (5분 단축)\n2. 설거지 시 물 받아서 하기\n3. 보일러 온수 온도 50도로 설정\n\n4인 가구 기준, 위 방법을 실천하면 월 15,000-25,000원 절약 가능합니다.");

        // 개인정보 로그
        createPrivacyLog(user, ActionType.DATA_COLLECTED, "사용량 수집", "전기/가스/수도 사용량 데이터 자동 수집");
        createPrivacyLog(user, ActionType.DATA_PROCESSED, "AI 분석", "사용 패턴 분석 및 맞춤형 추천 생성");
        createPrivacyLog(user, ActionType.DATA_PROCESSED, "챗봇 상담", "사용자 문의에 대한 AI 응답 생성");
    }

    // ============================================
    // Helper Methods
    // ============================================

    private void createElectricityBill(User user, LocalDate billingMonth, BigDecimal totalUsage,
            BigDecimal energyCharge, BigDecimal totalCharge, BigDecimal basicCharge, boolean isPaid) {
        BigDecimal climateCharge = totalCharge.multiply(new BigDecimal("0.05"));
        BigDecimal fuelCharge = totalCharge.multiply(new BigDecimal("0.03"));
        BigDecimal vat = totalCharge.multiply(new BigDecimal("0.10"));
        BigDecimal fund = totalCharge.multiply(new BigDecimal("0.037"));

        ElectricityBill bill = ElectricityBill.builder().user(user).billingMonth(billingMonth).basicCharge(basicCharge)
                .energyCharge(energyCharge).climateEnvironmentCharge(climateCharge).fuelAdjustmentCharge(fuelCharge)
                .vat(vat).electricIndustryFund(fund).totalCharge(totalCharge).totalUsage(totalUsage)
                .dueDate(billingMonth.plusDays(15)).isPaid(isPaid).build();
        electricityBillRepository.save(bill);
    }

    private void createGasBill(User user, LocalDate billingMonth, BigDecimal totalUsage, BigDecimal supplyPrice,
            BigDecimal totalCharge, boolean isPaid) {
        BigDecimal basicCharge = new BigDecimal("1000.00");
        BigDecimal cookingCharge = totalUsage.multiply(new BigDecimal("200"));
        BigDecimal heatingCharge = supplyPrice.subtract(cookingCharge);
        BigDecimal vat = supplyPrice.multiply(new BigDecimal("0.10"));

        GasBill bill = GasBill.builder().user(user).billingMonth(billingMonth).basicCharge(basicCharge)
                .cookingCharge(cookingCharge).heatingCharge(heatingCharge).supplyPrice(supplyPrice).vat(vat)
                .totalCharge(totalCharge).totalUsage(totalUsage).dueDate(billingMonth.plusDays(20)).isPaid(isPaid)
                .build();
        gasBillRepository.save(bill);
    }

    private void createWaterBill(User user, LocalDate billingMonth, BigDecimal totalUsage, BigDecimal totalCharge,
            boolean isPaid) {
        BigDecimal basicCharge = new BigDecimal("3000.00");
        BigDecimal waterSupply = totalCharge.multiply(new BigDecimal("0.40"));
        BigDecimal sewage = totalCharge.multiply(new BigDecimal("0.35"));
        BigDecimal waterUsage = totalCharge.multiply(new BigDecimal("0.25"));

        WaterBill bill = WaterBill.builder().user(user).billingMonth(billingMonth).basicCharge(basicCharge)
                .waterSupplyCharge(waterSupply).sewageCharge(sewage).waterUsageCharge(waterUsage)
                .totalCharge(totalCharge).totalUsage(totalUsage).dueDate(billingMonth.plusDays(25)).isPaid(isPaid)
                .build();
        waterBillRepository.save(bill);
    }

    private void createMonthlyBill(User user, UtilityType utilityType, LocalDate billingMonth, BigDecimal totalUsage,
            BigDecimal totalCharge, BigDecimal prevUsage, BigDecimal prevCharge, boolean isPaid) {
        MonthlyBill bill = MonthlyBill.builder().user(user).utilityType(utilityType).billingMonth(billingMonth)
                .totalUsage(totalUsage).totalCharge(totalCharge).previousMonthUsage(prevUsage)
                .previousMonthCharge(prevCharge).dueDate(billingMonth.plusDays(15)).isPaid(isPaid).build();
        monthlyBillRepository.save(bill);
    }

    private void createUsageData(User user, UtilityType utilityType, BigDecimal usageAmount, String unit,
            LocalDateTime measuredAt) {
        BigDecimal charge = switch (utilityType) {
            case ELECTRICITY -> usageAmount.multiply(new BigDecimal("180"));
            case GAS -> usageAmount.multiply(new BigDecimal("750"));
            case WATER -> usageAmount.multiply(new BigDecimal("1500"));
        };

        UsageData data = UsageData.builder().user(user).utilityType(utilityType).usageAmount(usageAmount).unit(unit)
                .currentCharge(charge).measuredAt(measuredAt).build();
        usageDataRepository.save(data);
    }

    private void createUsagePattern(User user, UtilityType utilityType, FrequencyType frequencyType,
            BigDecimal avgUsage, BigDecimal peakUsage, BigDecimal offPeakUsage, String trend) {
        UsagePattern pattern = UsagePattern.builder().user(user).utilityType(utilityType).frequencyType(frequencyType)
                .averageUsage(avgUsage).peakUsage(peakUsage).offPeakUsage(offPeakUsage).trend(trend).build();
        usagePatternRepository.save(pattern);
    }

    private Recommendation createRecommendation(User user, UtilityType utilityType, RecommendationType recType,
            String text, BigDecimal expectedSavings, String difficulty, boolean isApplied) {
        Recommendation rec = Recommendation.builder().user(user).utilityType(utilityType).recType(recType)
                .recommendationText(text).expectedSavings(expectedSavings).implementationDifficulty(difficulty)
                .isApplied(isApplied).build();
        return recommendationRepository.save(rec);
    }

    private void createSavingsTracking(User user, Recommendation recommendation, UtilityType utilityType,
            LocalDate trackingMonth, BigDecimal actualUsage, BigDecimal baselineCost, BigDecimal actualCost) {
        SavingsTracking tracking = SavingsTracking.builder().user(user).recommendation(recommendation)
                .utilityType(utilityType).trackingMonth(trackingMonth).actualUsage(actualUsage)
                .baselineCost(baselineCost).actualCost(actualCost).savingsAchieved(baselineCost.subtract(actualCost))
                .build();
        savingsTrackingRepository.save(tracking);
    }

    private void createUsageAlert(User user, UtilityType utilityType, AlertType alertType, String message,
            boolean isRead) {
        UsageAlert alert = UsageAlert.builder().user(user).utilityType(utilityType).alertType(alertType)
                .alertMessage(message).isRead(isRead).build();
        usageAlertRepository.save(alert);
    }

    private ChatSession createChatSession(User user, String title) {
        ChatSession session = ChatSession.builder().user(user).sessionTitle(title).isActive(true).build();
        return chatSessionRepository.save(session);
    }

    private void createChatMessage(ChatSession session, MessageRole role, String content) {
        ChatMessage message = ChatMessage.builder().chatSession(session).role(role).content(content)
                .tokensUsed(content.length() / 4) // 대략적인 토큰 계산
                .build();
        chatMessageRepository.save(message);
    }

    private void createPrivacyLog(User user, ActionType actionType, String accessType, String description) {
        PrivacyLog log = PrivacyLog.builder().user(user).actionType(actionType).accessType(accessType)
                .actionDescription(description).retentionPeriodDays(365)
                .deletionScheduledAt(LocalDateTime.now().plusDays(365)).isDeleted(false).build();
        privacyLogRepository.save(log);
    }
}
