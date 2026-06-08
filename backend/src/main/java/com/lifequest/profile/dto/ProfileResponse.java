package com.lifequest.profile.dto;

public record ProfileResponse(
        String goalType,
        String currentGoal,
        String goalPeriod,
        Double weeklyPlanHours,
        String currentStage,
        String feedbackStyle,
        Long routeId,
        String routeName,
        boolean completed
) {
}
