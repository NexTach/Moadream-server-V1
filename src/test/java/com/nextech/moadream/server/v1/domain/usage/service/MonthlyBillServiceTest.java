package com.nextech.moadream.server.v1.domain.usage.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillRequest;
import com.nextech.moadream.server.v1.domain.usage.dto.MonthlyBillResponse;
import com.nextech.moadream.server.v1.domain.usage.entity.MonthlyBill;
import com.nextech.moadream.server.v1.domain.usage.repository.MonthlyBillRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyBillService 테스트")
class MonthlyBillServiceTest {

    @Mock
    private MonthlyBillRepository monthlyBillRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MonthlyBillService monthlyBillService;

    private User testUser;
    private MonthlyBill testBill;
    private MonthlyBillRequest billRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("password")
                .name("테스트")
                .phone("010-1234-5678")
                .address("서울")
                .dateOfBirth("1990-01-01")
                .userVerificationCode("CODE")
                .build();
        ReflectionTestUtils.setField(testUser, "userId", 1L);

        LocalDate billingMonth = LocalDate.of(2025, 10, 1);

        testBill = MonthlyBill.builder()
                .user(testUser)
                .utilityType(UtilityType.ELECTRICITY)
                .billingMonth(billingMonth)
                .totalUsage(BigDecimal.valueOf(300.5))
                .totalCharge(BigDecimal.valueOf(45000))
                .previousMonthUsage(BigDecimal.valueOf(280.0))
                .previousMonthCharge(BigDecimal.valueOf(42000))
                .dueDate(LocalDate.of(2025, 11, 15))
                .isPaid(false)
                .build();
        ReflectionTestUtils.setField(testBill, "billId", 1L);

        billRequest = new MonthlyBillRequest(
                UtilityType.ELECTRICITY,
                billingMonth,
                BigDecimal.valueOf(300.5),
                BigDecimal.valueOf(45000),
                BigDecimal.valueOf(280.0),
                BigDecimal.valueOf(42000),
                LocalDate.of(2025, 11, 15)
        );
    }

    @Test
    @DisplayName("월별 요금 생성 성공")
    void createBill_Success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(monthlyBillRepository.save(any(MonthlyBill.class))).willReturn(testBill);

        // when
        MonthlyBillResponse result = monthlyBillService.createBill(1L, billRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        assertThat(result.getTotalCharge()).isEqualByComparingTo(BigDecimal.valueOf(45000));
        verify(userRepository).findById(1L);
        verify(monthlyBillRepository).save(any(MonthlyBill.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 요금 생성 실패")
    void createBill_UserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlyBillService.createBill(999L, billRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(monthlyBillRepository, never()).save(any(MonthlyBill.class));
    }

    @Test
    @DisplayName("사용자별 모든 요금 조회 성공")
    void getUserBills_Success() {
        // given
        List<MonthlyBill> bills = Arrays.asList(testBill);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(monthlyBillRepository.findByUser(any(User.class))).willReturn(bills);

        // when
        List<MonthlyBillResponse> result = monthlyBillService.getUserBills(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(userRepository).findById(1L);
        verify(monthlyBillRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("유틸리티 타입별 요금 조회 성공")
    void getUserBillsByType_Success() {
        // given
        List<MonthlyBill> bills = Arrays.asList(testBill);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(monthlyBillRepository.findByUserAndUtilityType(any(User.class), any(UtilityType.class)))
                .willReturn(bills);

        // when
        List<MonthlyBillResponse> result = monthlyBillService.getUserBillsByType(1L, UtilityType.ELECTRICITY);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUtilityType()).isEqualTo(UtilityType.ELECTRICITY);
        verify(monthlyBillRepository).findByUserAndUtilityType(testUser, UtilityType.ELECTRICITY);
    }

    @Test
    @DisplayName("특정 월 요금 조회 성공")
    void getBillByMonth_Success() {
        // given
        LocalDate billingMonth = LocalDate.of(2025, 10, 1);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(monthlyBillRepository.findByUserAndUtilityTypeAndBillingMonth(
                any(User.class), any(UtilityType.class), any(LocalDate.class)))
                .willReturn(Optional.of(testBill));

        // when
        MonthlyBillResponse result = monthlyBillService.getBillByMonth(1L, UtilityType.ELECTRICITY, billingMonth);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBillingMonth()).isEqualTo(billingMonth);
        verify(monthlyBillRepository).findByUserAndUtilityTypeAndBillingMonth(testUser, UtilityType.ELECTRICITY, billingMonth);
    }

    @Test
    @DisplayName("특정 월 요금이 없을 때 조회 실패")
    void getBillByMonth_NotFound() {
        // given
        LocalDate billingMonth = LocalDate.of(2025, 9, 1);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(testUser));
        given(monthlyBillRepository.findByUserAndUtilityTypeAndBillingMonth(
                any(User.class), any(UtilityType.class), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlyBillService.getBillByMonth(1L, UtilityType.ELECTRICITY, billingMonth))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BILL_NOT_FOUND);
    }

    @Test
    @DisplayName("요금 납부 처리 성공")
    void markBillAsPaid_Success() {
        // given
        given(monthlyBillRepository.findById(anyLong())).willReturn(Optional.of(testBill));

        // when
        MonthlyBillResponse result = monthlyBillService.markBillAsPaid(1L);

        // then
        assertThat(result).isNotNull();
        verify(monthlyBillRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 요금 납부 처리 실패")
    void markBillAsPaid_BillNotFound() {
        // given
        given(monthlyBillRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> monthlyBillService.markBillAsPaid(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BILL_NOT_FOUND);
    }
}