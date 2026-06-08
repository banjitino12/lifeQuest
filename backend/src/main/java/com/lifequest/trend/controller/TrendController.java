package com.lifequest.trend.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.trend.dto.TrendSummaryResponse;
import com.lifequest.trend.service.TrendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trends")
public class TrendController {

    private final TrendService trendService;

    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    @GetMapping("/summary")
    public ApiResponse<TrendSummaryResponse> summary() {
        return ApiResponse.ok(trendService.summary());
    }
}
