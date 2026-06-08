package com.lifequest.weekly.repository;

import com.lifequest.weekly.entity.WeeklyReportEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReportEntity, Long> {

    Optional<WeeklyReportEntity> findByUserIdAndWeekStartDateAndWeekEndDate(
            Long userId,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    );

    List<WeeklyReportEntity> findByUserIdOrderByWeekStartDateDesc(Long userId);
}
