package com.lifequest.trend.service;

import com.lifequest.trend.dto.TrendSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrendService {

    public TrendSummaryResponse summary() {
        return new TrendSummaryResponse(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
