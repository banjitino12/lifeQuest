package com.lifequest.gamification.repository;

import com.lifequest.common.enums.GameEventType;
import com.lifequest.gamification.entity.GameEventEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameEventRepository extends JpaRepository<GameEventEntity, Long> {

    List<GameEventEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<GameEventEntity> findByUserIdAndDailyLogId(Long userId, Long dailyLogId);

    List<GameEventEntity> findByUserIdAndEventType(Long userId, GameEventType eventType);

    List<GameEventEntity> findByUserIdAndLogDateBetweenOrderByLogDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
