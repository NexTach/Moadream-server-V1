package com.nextech.moadream.server.v1.global.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;

import com.nextech.moadream.server.v1.global.dto.ServerInfoDto;

@Service
public class ServerInfoService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public ServerInfoDto getServerInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        long totalMemoryMB = totalMemory / (1024 * 1024);
        long freeMemoryMB = freeMemory / (1024 * 1024);
        long usedMemoryMB = usedMemory / (1024 * 1024);
        long maxMemoryMB = maxMemory / (1024 * 1024);

        int memoryUsagePercent = (int) ((usedMemory * 100) / maxMemory);

        return ServerInfoDto.builder().applicationName(applicationName)
                .springBootVersion(SpringBootVersion.getVersion()).javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor")).totalMemoryMB(totalMemoryMB).freeMemoryMB(freeMemoryMB)
                .usedMemoryMB(usedMemoryMB).maxMemoryMB(maxMemoryMB).memoryUsagePercent(memoryUsagePercent)
                .serverTime(LocalDateTime.now()).profile(activeProfile)
                .availableProcessors(runtime.availableProcessors()).build();
    }
}
