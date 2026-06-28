package com.aicare.repository;

import com.aicare.model.TaskCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskCheckRepository extends JpaRepository<TaskCheck, Long> {

    List<TaskCheck> findByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    int countByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    Optional<TaskCheck> findByUserIdAndTopicIdAndTaskIndexAndCheckDate(
        Long userId, Long topicId, int taskIndex, LocalDate checkDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskCheck t WHERE t.userId = :userId AND t.checkDate < :date")
    void deleteByUserIdAndCheckDateBefore(Long userId, LocalDate date);

    @Query("SELECT COUNT(t) FROM TaskCheck t WHERE t.userId = :userId AND t.checkDate = :date")
    int countCheckedForDate(Long userId, LocalDate date);
}
