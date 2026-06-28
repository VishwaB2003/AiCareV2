package com.aicare.controller;

import com.aicare.dto.DailyTasksRequest;
import com.aicare.dto.DailyTasksResponse;
import com.aicare.dto.LearningRequest;
import com.aicare.dto.LearningResponse;
import com.aicare.dto.QuizRequest;
import com.aicare.dto.QuizResponse;
import com.aicare.service.LearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/ai")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @PostMapping("/explain")
    public ResponseEntity<LearningResponse> explain(@RequestBody LearningRequest req, Principal principal) {
        return ResponseEntity.ok(learningService.explain(req));
    }

    @PostMapping("/daily-tasks")
    public ResponseEntity<DailyTasksResponse> dailyTasks(@RequestBody DailyTasksRequest req, Principal principal) {
        return ResponseEntity.ok(learningService.generateDailyTasks(req));
    }

    @PostMapping("/quiz")
    public ResponseEntity<QuizResponse> quiz(@RequestBody QuizRequest req, Principal principal) {
        return ResponseEntity.ok(learningService.generateQuiz(req));
    }
}
