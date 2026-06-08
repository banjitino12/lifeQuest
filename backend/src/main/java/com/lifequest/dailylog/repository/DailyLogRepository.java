package com.lifequest.dailylog.repository;

import com.lifequest.dailylog.entity.DailyLogEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLogRepository extends JpaRepository<DailyLogEntity, Long> {

    Optional<DailyLogEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<DailyLogEntity> findByUserIdAndLogDateBetweenOrderByLogDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByUserIdAndLogDate(Long userId, LocalDate logDate);
}
