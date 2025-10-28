package com.nextech.moadream.server.v1.global.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ServerInfoDto {

    private String applicationName;
    private String springBootVersion;
    private String javaVersion;
    private String javaVendor;
    private long totalMemoryMB;
    private long freeMemoryMB;
    private long usedMemoryMB;
    private long maxMemoryMB;
    private int memoryUsagePercent;
    private LocalDateTime serverTime;
    private String profile;
    private int availableProcessors;
}