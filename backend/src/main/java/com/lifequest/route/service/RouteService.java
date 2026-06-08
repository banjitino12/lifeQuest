package com.lifequest.route.service;

import com.lifequest.route.dto.RouteSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    public List<RouteSummaryResponse> listRoutes() {
        return List.of(new RouteSummaryResponse(1L, "BACKEND_INTERN", "后端实习路线",
                "JOB_INTERVIEW", "从 Java 基础到技术面试的成长路线"));
    }
}
