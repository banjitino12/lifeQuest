package com.lifequest.route.repository;

import com.lifequest.route.entity.UserRouteProgressEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRouteProgressRepository extends JpaRepository<UserRouteProgressEntity, Long> {

    List<UserRouteProgressEntity> findByUserId(Long userId);

    Optional<UserRouteProgressEntity> findByUserIdAndRouteId(Long userId, Long routeId);
}
