package com.lifequest.settlement.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.dailylog.dto.SubmitDailyLogRequest;
import com.lifequest.settlement.dto.SettlementResponse;
import com.lifequest.settlement.service.SettlementService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ApiResponse<SettlementResponse> submitAndSettle(@Valid @RequestBody SubmitDailyLogRequest request) {
        return ApiResponse.created(settlementService.submitAndSettle(request));
    }

    @GetMapping("/by-date")
    public ApiResponse<SettlementResponse> getByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(settlementService.getByDate(date));
    }
}
