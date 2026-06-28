package com.aicare.controller;

import com.aicare.dto.*;
import com.aicare.model.StudyTopic;
import com.aicare.service.StudyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    // Load dashboard (topics + today's checks + stats + pending summary)
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Principal principal) {
        return ResponseEntity.ok(studyService.getDashboard(principal.getName()));
    }

    // Create a new topic
    @PostMapping("/topics")
    public ResponseEntity<StudyTopic> createTopic(@Valid @RequestBody TopicRequest req, Principal principal) {
        return ResponseEntity.ok(studyService.saveTopic(principal.getName(), req));
    }

    // Delete a topic
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id, Principal principal) {
        studyService.deleteTopic(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // Check or uncheck a task
    @PostMapping("/check")
    public ResponseEntity<Void> checkTask(@RequestBody CheckRequest req, Principal principal) {
        studyService.setTaskCheck(principal.getName(), req);
        return ResponseEntity.ok().build();
    }
}
