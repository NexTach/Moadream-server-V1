package com.nextech.moadream.server.v1.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextech.moadream.server.v1.global.dto.ServerInfoDto;
import com.nextech.moadream.server.v1.global.service.DatabaseViewerService;
import com.nextech.moadream.server.v1.global.service.ServerInfoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ServerInfoService serverInfoService;
    private final DatabaseViewerService databaseViewerService;

    @GetMapping("/")
    public String index(Model model) {
        ServerInfoDto serverInfo = serverInfoService.getServerInfo();
        model.addAttribute("serverInfo", serverInfo);
        return "index";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/data")
    public String data(Model model) {
        model.addAttribute("statistics", databaseViewerService.getDatabaseStatistics());
        model.addAttribute("allData", databaseViewerService.getAllDatabaseData());
        return "data";
    }

    @GetMapping("/api/server-info")
    public String getServerInfo(Model model) {
        ServerInfoDto serverInfo = serverInfoService.getServerInfo();
        model.addAttribute("serverInfo", serverInfo);
        return "fragments/server-stats :: stats";
    }
}
