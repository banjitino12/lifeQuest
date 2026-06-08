package com.lifequest.route.repository;

import com.lifequest.route.entity.RouteLevelEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteLevelRepository extends JpaRepository<RouteLevelEntity, Long> {

    List<RouteLevelEntity> findByRouteIdOrderByLevelNoAsc(Long routeId);

    List<RouteLevelEntity> findByChapterIdOrderByLevelNoAsc(Long chapterId);

    Optional<RouteLevelEntity> findByChapterIdAndLevelNo(Long chapterId, Integer levelNo);
}
