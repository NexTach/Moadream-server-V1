package com.nextech.moadream.server.v1.domain.usage.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 20)
    private AlertType alertType;

    @Column(name = "alert_message", nullable = false, columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UsageAlert(User user, UtilityType utilityType, AlertType alertType, String alertMessage, Boolean isRead) {
        this.user = user;
        this.utilityType = utilityType;
        this.alertType = alertType;
        this.alertMessage = alertMessage;
        this.isRead = isRead != null ? isRead : false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
