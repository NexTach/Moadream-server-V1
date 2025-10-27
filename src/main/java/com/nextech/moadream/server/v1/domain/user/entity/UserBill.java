package com.nextech.moadream.server.v1.domain.user.entity;

import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_bills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBill {

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

    @Column(name = "bill_number", nullable = false, unique = true)
    private String billNumber;

    @Column(name = "generation_name", length = 100)
    private String generationName;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserBill(User user, UtilityType utilityType, String billNumber,
                    String generationName, Boolean isVerified) {
        this.user = user;
        this.utilityType = utilityType;
        this.billNumber = billNumber;
        this.generationName = generationName;
        this.isVerified = isVerified != null ? isVerified : false;
    }

    public void verify() {
        this.isVerified = true;
    }
}