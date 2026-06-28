package com.aicare.repository;

import com.aicare.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUserId(Long userId);
}
