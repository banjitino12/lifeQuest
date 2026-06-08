package com.lifequest.route.repository;

import com.lifequest.common.enums.GoalType;
import com.lifequest.route.entity.GrowthRouteEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrowthRouteRepository extends JpaRepository<GrowthRouteEntity, Long> {

    Optional<GrowthRouteEntity> findByRouteCode(String routeCode);

    List<GrowthRouteEntity> findByGoalType(GoalType goalType);

    Optional<GrowthRouteEntity> findByGoalTypeAndDefaultRouteTrue(GoalType goalType);
}
