package com.lifequest.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ProfileRequest(
        @NotBlank String goalType,
        @NotBlank String currentGoal,
        String goalPeriod,
        @PositiveOrZero Double weeklyPlanHours,
        String currentStage,
        @NotBlank String feedbackStyle,
        Long routeId
) {
}
