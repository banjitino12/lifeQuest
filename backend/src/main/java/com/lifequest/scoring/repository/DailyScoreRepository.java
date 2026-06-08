package com.lifequest.scoring.repository;

import com.lifequest.scoring.entity.DailyScoreEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyScoreRepository extends JpaRepository<DailyScoreEntity, Long> {

    Optional<DailyScoreEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    Optional<DailyScoreEntity> findByUserIdAndDailyLogId(Long userId, Long dailyLogId);

    List<DailyScoreEntity> findByUserIdAndLogDateBetweenOrderByLogDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
