package com.lifequest.route.repository;

import com.lifequest.route.entity.RouteChapterEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteChapterRepository extends JpaRepository<RouteChapterEntity, Long> {

    List<RouteChapterEntity> findByRouteIdOrderByChapterNoAsc(Long routeId);

    Optional<RouteChapterEntity> findByRouteIdAndChapterNo(Long routeId, Integer chapterNo);
}
