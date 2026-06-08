package com.lifequest.route.controller;

import com.lifequest.common.response.ApiResponse;
import com.lifequest.route.dto.RouteSummaryResponse;
import com.lifequest.route.service.RouteService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ApiResponse<List<RouteSummaryResponse>> listRoutes() {
        return ApiResponse.ok(routeService.listRoutes());
    }
}
