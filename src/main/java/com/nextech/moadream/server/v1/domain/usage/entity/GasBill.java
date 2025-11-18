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
@Table(name = "gas_bills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GasBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gas_bill_id")
    private Long gasBillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "basic_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicCharge; // 기본 요금

    @Column(name = "cooking_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal cookingCharge; // 취사 요금

    @Column(name = "heating_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal heatingCharge; // 난방 요금

    @Column(name = "supply_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal supplyPrice; // 공급가액

    @Column(name = "vat", nullable = false, precision = 10, scale = 2)
    private BigDecimal vat; // 부가세

    @Column(name = "total_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCharge; // 총 요금

    @Column(name = "total_usage", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUsage; // 총 사용량 (m³)

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Builder
    public GasBill(User user, LocalDate billingMonth, BigDecimal basicCharge, BigDecimal cookingCharge,
            BigDecimal heatingCharge, BigDecimal supplyPrice, BigDecimal vat, BigDecimal totalCharge,
            BigDecimal totalUsage, LocalDate dueDate, Boolean isPaid) {
        this.user = user;
        this.billingMonth = billingMonth;
        this.basicCharge = basicCharge;
        this.cookingCharge = cookingCharge;
        this.heatingCharge = heatingCharge;
        this.supplyPrice = supplyPrice;
        this.vat = vat;
        this.totalCharge = totalCharge;
        this.totalUsage = totalUsage;
        this.dueDate = dueDate;
        this.isPaid = isPaid != null ? isPaid : false;
    }

    public void markAsPaid() {
        this.isPaid = true;
    }

    public void calculateTotalCharge() {
        this.totalCharge = this.basicCharge.add(this.cookingCharge).add(this.heatingCharge).add(this.supplyPrice)
                .add(this.vat);
    }
}
