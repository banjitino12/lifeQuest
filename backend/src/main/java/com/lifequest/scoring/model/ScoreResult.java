package com.lifequest.scoring.model;

import java.math.BigDecimal;
import java.util.Map;

public record ScoreResult(
        BigDecimal dailyScore,
        String rating,
        BigDecimal growthScore,
        BigDecimal executionScore,
        BigDecimal energyScore,
        BigDecimal moodScore,
        BigDecimal distractionScore,
        BigDecimal reflectionScore,
        Map<String, String> reasons
) {
}
