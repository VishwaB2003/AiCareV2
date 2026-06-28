package com.aicare.repository;

import com.aicare.model.StudyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyTopicRepository extends JpaRepository<StudyTopic, Long> {
    List<StudyTopic> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
