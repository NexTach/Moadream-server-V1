package com.nextech.moadream.server.v1.domain.usage.dto;

import com.nextech.moadream.server.v1.domain.usage.enums.AlertType;
import com.nextech.moadream.server.v1.domain.user.enums.UtilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용량 알림 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsageAlertRequest {

    @Schema(description = "사용량 유형", example = "ELECTRICITY")
    @NotNull(message = "사용량 유형은 필수입니다.")
    private UtilityType utilityType;

    @Schema(description = "알림 유형", example = "USAGE_THRESHOLD")
    @NotNull(message = "알림 유형은 필수입니다.")
    private AlertType alertType;

    @Schema(description = "알림 메시지", example = "전기 사용량이 설정한 임계값을 초과했습니다.")
    @NotBlank(message = "알림 메시지는 필수입니다.")
    private String alertMessage;
}