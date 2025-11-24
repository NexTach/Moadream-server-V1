package com.nextech.moadream.server.v1.global.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextech.moadream.server.v1.domain.analysis.entity.Recommendation;
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
import com.nextech.moadream.server.v1.global.config.dto.MockDataConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class JsonMockDataLoader implements ApplicationRunner {

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
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void run(ApplicationArguments args) {
        if (isDatabaseAlreadyInitialized()) {
            log.info("Mock data already exists. Skipping data initialization.");
            log.info("Database statistics - Users: {}, UsageData: {}, ElectricityBills: {}", userRepository.count(),
                    usageDataRepository.count(), electricityBillRepository.count());
            return;
        }

        log.info("Starting JSON-based mock data initialization...");

        try {
            List<MockDataConfig> userConfigs = loadMockDataFromJson();

            for (MockDataConfig config : userConfigs) {
                User user = createUser(config);
                createUserData(user, config);
            }

            log.info("Mock data initialization completed successfully! Loaded {} users.", userConfigs.size());
            log.info("Final database statistics - Users: {}, UsageData: {}, ChatSessions: {}", userRepository.count(),
                    usageDataRepository.count(), chatSessionRepository.count());
        } catch (Exception e) {
            log.error("Failed to load mock data from JSON", e);
            throw new RuntimeException("Mock data initialization failed", e);
        }
    }

    private boolean isDatabaseAlreadyInitialized() {
        long userCount = userRepository.count();
        long usageDataCount = usageDataRepository.count();
        long electricityBillCount = electricityBillRepository.count();

        boolean hasUsers = userCount > 0;
        boolean hasRelatedData = usageDataCount > 0 || electricityBillCount > 0;

        return hasUsers && hasRelatedData;
    }

    private List<MockDataConfig> loadMockDataFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("mock-data/users.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<MockDataConfig>>() {
            });
        }
    }

    private User createUser(MockDataConfig config) {
        User user = User.builder().email(config.getEmail()).passwordHash(passwordEncoder.encode(config.getPassword()))
                .name(config.getName()).phone(config.getPhone()).address(config.getAddress())
                .dateOfBirth(config.getDateOfBirth()).userVerificationCode(config.getUserVerificationCode())
                .provider(config.getProvider()).providerId(null).build();
        return userRepository.save(user);
    }

    private void createUserData(User user, MockDataConfig config) {
        if (config.getSettings() != null) {
            MockDataConfig.UserSettingDto settingDto = config.getSettings();
            UserSetting setting = UserSetting.builder().user(user).monthlyBudget(settingDto.getMonthlyBudget())
                    .alertThreshold(settingDto.getAlertThreshold()).pushEnabled(settingDto.getPushEnabled())
                    .emailEnabled(settingDto.getEmailEnabled()).efficiencyScore(settingDto.getEfficiencyScore())
                    .build();
            userSettingRepository.save(setting);
        }

        if (config.getBills() != null) {
            for (MockDataConfig.UserBillDto billDto : config.getBills()) {
                UserBill bill = UserBill.builder().user(user).utilityType(UtilityType.valueOf(billDto.getUtilityType()))
                        .billNumber(billDto.getBillNumber()).generationName(billDto.getGenerationName())
                        .isVerified(billDto.getIsVerified()).build();
                userBillRepository.save(bill);
            }
        }

        if (config.getElectricityBills() != null) {
            for (MockDataConfig.ElectricityBillDto billDto : config.getElectricityBills()) {
                createElectricityBill(user, billDto);
            }
        }

        if (config.getGasBills() != null) {
            for (MockDataConfig.GasBillDto billDto : config.getGasBills()) {
                createGasBill(user, billDto);
            }
        }

        if (config.getWaterBills() != null) {
            for (MockDataConfig.WaterBillDto billDto : config.getWaterBills()) {
                createWaterBill(user, billDto);
            }
        }

        createUsageData(user);

        if (config.getUsagePatterns() != null) {
            for (MockDataConfig.UsagePatternDto patternDto : config.getUsagePatterns()) {
                UsagePattern pattern = UsagePattern.builder().user(user)
                        .utilityType(UtilityType.valueOf(patternDto.getUtilityType()))
                        .frequencyType(FrequencyType.valueOf(patternDto.getFrequencyType()))
                        .averageUsage(patternDto.getAverageUsage()).peakUsage(patternDto.getPeakUsage())
                        .offPeakUsage(patternDto.getOffPeakUsage()).trend(patternDto.getTrend()).build();
                usagePatternRepository.save(pattern);
            }
        }

        if (config.getRecommendations() != null) {
            for (MockDataConfig.RecommendationDto recDto : config.getRecommendations()) {
                Recommendation rec = Recommendation.builder().user(user)
                        .utilityType(UtilityType.valueOf(recDto.getUtilityType()))
                        .recType(RecommendationType.valueOf(recDto.getRecType()))
                        .recommendationText(recDto.getRecommendationText()).expectedSavings(recDto.getExpectedSavings())
                        .implementationDifficulty(recDto.getImplementationDifficulty()).isApplied(recDto.getIsApplied())
                        .build();
                recommendationRepository.save(rec);
            }
        }

        if (config.getAlerts() != null) {
            for (MockDataConfig.AlertDto alertDto : config.getAlerts()) {
                UsageAlert alert = UsageAlert.builder().user(user)
                        .utilityType(UtilityType.valueOf(alertDto.getUtilityType()))
                        .alertType(AlertType.valueOf(alertDto.getAlertType())).alertMessage(alertDto.getAlertMessage())
                        .isRead(alertDto.getIsRead()).build();
                usageAlertRepository.save(alert);
            }
        }

        // 채팅 세션
        if (config.getChatSessions() != null) {
            for (MockDataConfig.ChatSessionDto sessionDto : config.getChatSessions()) {
                ChatSession session = ChatSession.builder().user(user).sessionTitle(sessionDto.getSessionTitle())
                        .isActive(true).build();
                session = chatSessionRepository.save(session);

                if (sessionDto.getMessages() != null) {
                    for (MockDataConfig.ChatMessageDto msgDto : sessionDto.getMessages()) {
                        ChatMessage message = ChatMessage.builder().chatSession(session)
                                .role(MessageRole.valueOf(msgDto.getRole())).content(msgDto.getContent())
                                .tokensUsed(msgDto.getContent().length() / 4).build();
                        chatMessageRepository.save(message);
                    }
                }
            }
        }

        createPrivacyLogs(user);
    }

    private void createElectricityBill(User user, MockDataConfig.ElectricityBillDto dto) {
        LocalDate billingMonth = LocalDate.now().minusMonths(dto.getMonthsAgo()).withDayOfMonth(1);
        BigDecimal climateCharge = dto.getTotalCharge().multiply(new BigDecimal("0.05"));
        BigDecimal fuelCharge = dto.getTotalCharge().multiply(new BigDecimal("0.03"));
        BigDecimal vat = dto.getTotalCharge().multiply(new BigDecimal("0.10"));
        BigDecimal fund = dto.getTotalCharge().multiply(new BigDecimal("0.037"));

        ElectricityBill bill = ElectricityBill.builder().user(user).billingMonth(billingMonth)
                .basicCharge(dto.getBasicCharge()).energyCharge(dto.getEnergyCharge())
                .climateEnvironmentCharge(climateCharge).fuelAdjustmentCharge(fuelCharge).vat(vat)
                .electricIndustryFund(fund).totalCharge(dto.getTotalCharge()).totalUsage(dto.getTotalUsage())
                .dueDate(billingMonth.plusDays(15)).isPaid(dto.getIsPaid()).build();
        electricityBillRepository.save(bill);

        // MonthlyBill 생성
        LocalDate previousMonth = billingMonth.minusMonths(1);
        MonthlyBill previousMonthlyBill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, UtilityType.ELECTRICITY, previousMonth).orElse(null);

        MonthlyBill monthlyBill = MonthlyBill.builder()
                .user(user)
                .utilityType(UtilityType.ELECTRICITY)
                .billingMonth(billingMonth)
                .totalUsage(dto.getTotalUsage())
                .totalCharge(dto.getTotalCharge())
                .previousMonthUsage(previousMonthlyBill != null ? previousMonthlyBill.getTotalUsage() : null)
                .previousMonthCharge(previousMonthlyBill != null ? previousMonthlyBill.getTotalCharge() : null)
                .dueDate(billingMonth.plusDays(15))
                .isPaid(dto.getIsPaid())
                .build();
        monthlyBillRepository.save(monthlyBill);
    }

    private void createGasBill(User user, MockDataConfig.GasBillDto dto) {
        LocalDate billingMonth = LocalDate.now().minusMonths(dto.getMonthsAgo()).withDayOfMonth(1);
        BigDecimal basicCharge = new BigDecimal("1000.00");
        BigDecimal cookingCharge = dto.getTotalUsage().multiply(new BigDecimal("200"));
        BigDecimal heatingCharge = dto.getSupplyPrice().subtract(cookingCharge);
        BigDecimal vat = dto.getSupplyPrice().multiply(new BigDecimal("0.10"));

        GasBill bill = GasBill.builder().user(user).billingMonth(billingMonth).basicCharge(basicCharge)
                .cookingCharge(cookingCharge).heatingCharge(heatingCharge).supplyPrice(dto.getSupplyPrice()).vat(vat)
                .totalCharge(dto.getTotalCharge()).totalUsage(dto.getTotalUsage()).dueDate(billingMonth.plusDays(20))
                .isPaid(dto.getIsPaid()).build();
        gasBillRepository.save(bill);

        // MonthlyBill 생성
        LocalDate previousMonth = billingMonth.minusMonths(1);
        MonthlyBill previousMonthlyBill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, UtilityType.GAS, previousMonth).orElse(null);

        MonthlyBill monthlyBill = MonthlyBill.builder()
                .user(user)
                .utilityType(UtilityType.GAS)
                .billingMonth(billingMonth)
                .totalUsage(dto.getTotalUsage())
                .totalCharge(dto.getTotalCharge())
                .previousMonthUsage(previousMonthlyBill != null ? previousMonthlyBill.getTotalUsage() : null)
                .previousMonthCharge(previousMonthlyBill != null ? previousMonthlyBill.getTotalCharge() : null)
                .dueDate(billingMonth.plusDays(20))
                .isPaid(dto.getIsPaid())
                .build();
        monthlyBillRepository.save(monthlyBill);
    }

    private void createWaterBill(User user, MockDataConfig.WaterBillDto dto) {
        LocalDate billingMonth = LocalDate.now().minusMonths(dto.getMonthsAgo()).withDayOfMonth(1);
        BigDecimal basicCharge = new BigDecimal("3000.00");
        BigDecimal waterSupply = dto.getTotalCharge().multiply(new BigDecimal("0.40"));
        BigDecimal sewage = dto.getTotalCharge().multiply(new BigDecimal("0.35"));
        BigDecimal waterUsage = dto.getTotalCharge().multiply(new BigDecimal("0.25"));

        WaterBill bill = WaterBill.builder().user(user).billingMonth(billingMonth).basicCharge(basicCharge)
                .waterSupplyCharge(waterSupply).sewageCharge(sewage).waterUsageCharge(waterUsage)
                .totalCharge(dto.getTotalCharge()).totalUsage(dto.getTotalUsage()).dueDate(billingMonth.plusDays(25))
                .isPaid(dto.getIsPaid()).build();
        waterBillRepository.save(bill);

        // MonthlyBill 생성
        LocalDate previousMonth = billingMonth.minusMonths(1);
        MonthlyBill previousMonthlyBill = monthlyBillRepository
                .findByUserAndUtilityTypeAndBillingMonth(user, UtilityType.WATER, previousMonth).orElse(null);

        MonthlyBill monthlyBill = MonthlyBill.builder()
                .user(user)
                .utilityType(UtilityType.WATER)
                .billingMonth(billingMonth)
                .totalUsage(dto.getTotalUsage())
                .totalCharge(dto.getTotalCharge())
                .previousMonthUsage(previousMonthlyBill != null ? previousMonthlyBill.getTotalUsage() : null)
                .previousMonthCharge(previousMonthlyBill != null ? previousMonthlyBill.getTotalCharge() : null)
                .dueDate(billingMonth.plusDays(25))
                .isPaid(dto.getIsPaid())
                .build();
        monthlyBillRepository.save(monthlyBill);
    }

    private void createUsageData(User user) {
        for (int i = 30; i >= 0; i--) {
            LocalDateTime measuredAt = LocalDateTime.now().minusDays(i);

            BigDecimal elecUsage = new BigDecimal(5.5 + Math.random() * 2);
            UsageData elecData = UsageData.builder().user(user).utilityType(UtilityType.ELECTRICITY)
                    .usageAmount(elecUsage).unit("kWh").currentCharge(elecUsage.multiply(new BigDecimal("180")))
                    .measuredAt(measuredAt).build();
            usageDataRepository.save(elecData);

            if (i % 3 == 0) {
                BigDecimal gasUsage = new BigDecimal(0.8 + Math.random() * 0.5);
                UsageData gasData = UsageData.builder().user(user).utilityType(UtilityType.GAS).usageAmount(gasUsage)
                        .unit("m³").currentCharge(gasUsage.multiply(new BigDecimal("750"))).measuredAt(measuredAt)
                        .build();
                usageDataRepository.save(gasData);
            }

            if (i % 7 == 0) {
                BigDecimal waterUsage = new BigDecimal(0.2 + Math.random() * 0.15);
                UsageData waterData = UsageData.builder().user(user).utilityType(UtilityType.WATER)
                        .usageAmount(waterUsage).unit("m³").currentCharge(waterUsage.multiply(new BigDecimal("1500")))
                        .measuredAt(measuredAt).build();
                usageDataRepository.save(waterData);
            }
        }
    }

    private void createPrivacyLogs(User user) {
        PrivacyLog log1 = PrivacyLog.builder().user(user).actionType(ActionType.DATA_COLLECTED).accessType("사용량 수집")
                .actionDescription("전기/가스/수도 사용량 데이터 자동 수집").retentionPeriodDays(365)
                .deletionScheduledAt(LocalDateTime.now().plusDays(365)).isDeleted(false).build();
        privacyLogRepository.save(log1);

        PrivacyLog log2 = PrivacyLog.builder().user(user).actionType(ActionType.DATA_PROCESSED).accessType("AI 분석")
                .actionDescription("사용 패턴 분석 및 맞춤형 추천 생성").retentionPeriodDays(365)
                .deletionScheduledAt(LocalDateTime.now().plusDays(365)).isDeleted(false).build();
        privacyLogRepository.save(log2);
    }
}
