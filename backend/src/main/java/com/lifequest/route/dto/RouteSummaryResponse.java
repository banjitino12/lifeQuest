package com.lifequest.route.dto;

public record RouteSummaryResponse(
        Long routeId,
        String routeCode,
        String routeName,
        String goalType,
        String description
) {
}
