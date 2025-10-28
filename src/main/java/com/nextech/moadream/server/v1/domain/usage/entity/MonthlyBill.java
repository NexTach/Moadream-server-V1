package com.nextech.moadream.server.v1.domain.usage.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monthly_bills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "total_usage", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUsage;

    @Column(name = "total_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCharge;

    @Column(name = "previous_month_usage", precision = 10, scale = 2)
    private BigDecimal previousMonthUsage;

    @Column(name = "previous_month_charge", precision = 10, scale = 2)
    private BigDecimal previousMonthCharge;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Builder
    public MonthlyBill(User user, UtilityType utilityType, LocalDate billingMonth, BigDecimal totalUsage,
            BigDecimal totalCharge, BigDecimal previousMonthUsage, BigDecimal previousMonthCharge, LocalDate dueDate,
            Boolean isPaid) {
        this.user = user;
        this.utilityType = utilityType;
        this.billingMonth = billingMonth;
        this.totalUsage = totalUsage;
        this.totalCharge = totalCharge;
        this.previousMonthUsage = previousMonthUsage;
        this.previousMonthCharge = previousMonthCharge;
        this.dueDate = dueDate;
        this.isPaid = isPaid != null ? isPaid : false;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }
}
