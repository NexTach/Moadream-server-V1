package com.nextech.moadream.server.v1.domain.usage.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextech.moadream.server.v1.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "water_bills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaterBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "water_bill_id")
    private Long waterBillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "basic_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicCharge; // 기본 요금

    @Column(name = "water_supply_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterSupplyCharge; // 상수도 요금 (기본)

    @Column(name = "sewage_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal sewageCharge; // 하수도 요금

    @Column(name = "water_usage_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal waterUsageCharge; // 물이용부담금

    @Column(name = "total_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCharge; // 총 요금

    @Column(name = "total_usage", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUsage; // 총 사용량 (m³)

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Builder
    public WaterBill(User user, LocalDate billingMonth, BigDecimal basicCharge, BigDecimal waterSupplyCharge,
            BigDecimal sewageCharge, BigDecimal waterUsageCharge, BigDecimal totalCharge, BigDecimal totalUsage,
            LocalDate dueDate, Boolean isPaid) {
        this.user = user;
        this.billingMonth = billingMonth;
        this.basicCharge = basicCharge;
        this.waterSupplyCharge = waterSupplyCharge;
        this.sewageCharge = sewageCharge;
        this.waterUsageCharge = waterUsageCharge;
        this.totalCharge = totalCharge;
        this.totalUsage = totalUsage;
        this.dueDate = dueDate;
        this.isPaid = isPaid != null ? isPaid : false;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }

    public void calculateTotalCharge() {
        this.totalCharge = this.basicCharge.add(this.waterSupplyCharge).add(this.sewageCharge)
                .add(this.waterUsageCharge);
    }
}
