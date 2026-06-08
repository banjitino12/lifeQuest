package com.lifequest.dailylog.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.dailylog.dto.DailyLogSummaryResponse;
import com.lifequest.dailylog.dto.SubmitDailyLogRequest;
import com.lifequest.dailylog.service.DailyLogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-logs")
public class DailyLogController {

    private final DailyLogService dailyLogService;

    public DailyLogController(DailyLogService dailyLogService) {
        this.dailyLogService = dailyLogService;
    }

    @PostMapping
    public ApiResponse<DailyLogSummaryResponse> submit(@Valid @RequestBody SubmitDailyLogRequest request) {
        return ApiResponse.created(dailyLogService.submit(request));
    }
}
