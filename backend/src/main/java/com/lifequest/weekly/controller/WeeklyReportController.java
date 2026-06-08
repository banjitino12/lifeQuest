package com.lifequest.weekly.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.weekly.dto.WeeklyReportSummaryResponse;
import com.lifequest.weekly.service.WeeklyReportService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weekly-reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @GetMapping
    public ApiResponse<List<WeeklyReportSummaryResponse>> listReports() {
        return ApiResponse.ok(weeklyReportService.listReports());
    }
}
