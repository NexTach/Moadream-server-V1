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
@Table(name = "electricity_bills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ElectricityBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "electricity_bill_id")
    private Long electricityBillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "basic_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicCharge;

    @Column(name = "energy_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal energyCharge;

    @Column(name = "climate_environment_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal climateEnvironmentCharge;

    @Column(name = "fuel_adjustment_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal fuelAdjustmentCharge;

    @Column(name = "vat", nullable = false, precision = 10, scale = 2)
    private BigDecimal vat;

    @Column(name = "electric_industry_fund", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricIndustryFund;

    @Column(name = "total_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCharge;

    @Column(name = "total_usage", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUsage;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Builder
    public ElectricityBill(User user, LocalDate billingMonth, BigDecimal basicCharge, BigDecimal energyCharge,
            BigDecimal climateEnvironmentCharge, BigDecimal fuelAdjustmentCharge, BigDecimal vat,
            BigDecimal electricIndustryFund, BigDecimal totalCharge, BigDecimal totalUsage, LocalDate dueDate,
            Boolean isPaid) {
        this.user = user;
        this.billingMonth = billingMonth;
        this.basicCharge = basicCharge;
        this.energyCharge = energyCharge;
        this.climateEnvironmentCharge = climateEnvironmentCharge;
        this.fuelAdjustmentCharge = fuelAdjustmentCharge;
        this.vat = vat;
        this.electricIndustryFund = electricIndustryFund;
        this.totalCharge = totalCharge;
        this.totalUsage = totalUsage;
        this.dueDate = dueDate;
        this.isPaid = isPaid != null ? isPaid : false;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }

    public void calculateTotalCharge() {
        this.totalCharge = this.basicCharge.add(this.energyCharge).add(this.climateEnvironmentCharge)
                .add(this.fuelAdjustmentCharge).add(this.vat).add(this.electricIndustryFund);
    }
}
