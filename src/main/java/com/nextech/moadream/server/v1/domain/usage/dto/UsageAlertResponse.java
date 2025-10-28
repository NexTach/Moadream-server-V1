package com.nextech.moadream.server.v1.domain.usage.dto;

import java.time.LocalDateTime;

import com.nextech.moadream.server.v1.domain.usage.entity.UsageAlert;
import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "사용량 알림 응답")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UsageAlertResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long alertId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    private UtilityType utilityType;

    @Schema(description = "알림 유형", example = "USAGE_THRESHOLD")
    private AlertType alertType;

    @Schema(description = "알림 메시지", example = "전기 사용량이 설정한 임계값을 초과했습니다.")
    private String alertMessage;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "생성 시간", example = "2025-01-27T15:30:00")
    private LocalDateTime createdAt;

    public static UsageAlertResponse from(UsageAlert alert) {
        return UsageAlertResponse.builder().alertId(alert.getAlertId()).userId(alert.getUser().getUserId())
                .utilityType(alert.getUtilityType()).alertType(alert.getAlertType())
                .alertMessage(alert.getAlertMessage()).isRead(alert.getIsRead()).createdAt(alert.getCreatedAt())
                .build();
    }
}
