package com.aicare.service;

import com.aicare.dto.*;
import com.aicare.model.*;
import com.aicare.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudyService {

    private final StudyTopicRepository topicRepo;
    private final TaskCheckRepository checkRepo;
    private final UserStatsRepository statsRepo;
    private final UserRepository userRepo;

    public StudyService(StudyTopicRepository topicRepo, TaskCheckRepository checkRepo,
                        UserStatsRepository statsRepo, UserRepository userRepo) {
        this.topicRepo = topicRepo;
        this.checkRepo = checkRepo;
        this.statsRepo = statsRepo;
        this.userRepo = userRepo;
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @Transactional
    public DashboardResponse getDashboard(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        UserStats stats = getOrCreateStats(user.getId());
        LocalDate today = LocalDate.now();

        SummaryResponse pendingSummary = null;

        // New day detected → calculate yesterday's score, reset checks
        if (stats.getLastActiveDate() != null && !stats.getLastActiveDate().equals(today)) {
            pendingSummary = calculateAndApplyDayScore(user.getId(), stats, stats.getLastActiveDate());
            checkRepo.deleteByUserIdAndCheckDateBefore(user.getId(), today);
        }
        stats.setLastActiveDate(today);
        statsRepo.save(stats);

        List<StudyTopic> topics = topicRepo.findByUserId(user.getId());
        List<TaskCheck> todayChecks = checkRepo.findByUserIdAndCheckDate(user.getId(), today);

        // Build map: topicId -> [taskIndexes]
        Map<Long, List<Integer>> checkedMap = new HashMap<>();
        for (TaskCheck tc : todayChecks) {
            checkedMap.computeIfAbsent(tc.getTopicId(), k -> new ArrayList<>()).add(tc.getTaskIndex());
        }

        DashboardResponse resp = new DashboardResponse();
        resp.setTopics(topics);
        resp.setCheckedTasks(checkedMap);
        resp.setStats(toStatsResponse(stats));
        resp.setPendingSummary(pendingSummary);
        return resp;
    }

    // ── TOPICS ────────────────────────────────────────────────────────────────

    public StudyTopic saveTopic(String email, TopicRequest req) {
        User user = userRepo.findByEmail(email).orElseThrow();
        StudyTopic topic = new StudyTopic();
        topic.setUserId(user.getId());
        topic.setName(req.getName());
        topic.setDescription(req.getDescription());
        topic.setTotalDays(req.getTotalDays());
        topic.setDailyHours(req.getDailyHours());
        topic.setColor(req.getColor());
        topic.setSubTasks(req.getSubTasks());
        return topicRepo.save(topic);
    }

    @Transactional
    public void deleteTopic(String email, Long topicId) {
        User user = userRepo.findByEmail(email).orElseThrow();
        topicRepo.deleteByIdAndUserId(topicId, user.getId());
    }

    // ── TASK CHECK/UNCHECK ────────────────────────────────────────────────────

    @Transactional
    public void setTaskCheck(String email, CheckRequest req) {
        User user = userRepo.findByEmail(email).orElseThrow();
        LocalDate today = LocalDate.now();
        Optional<TaskCheck> existing = checkRepo.findByUserIdAndTopicIdAndTaskIndexAndCheckDate(
                user.getId(), req.getTopicId(), req.getTaskIndex(), today);

        if (req.isChecked()) {
            if (existing.isEmpty()) {
                checkRepo.save(new TaskCheck(user.getId(), req.getTopicId(), req.getTaskIndex(), today));
            }
        } else {
            existing.ifPresent(checkRepo::delete);
        }
    }

    // ── SCORING ALGORITHM ─────────────────────────────────────────────────────

    private SummaryResponse calculateAndApplyDayScore(Long userId, UserStats stats, LocalDate date) {
        List<StudyTopic> topics = topicRepo.findByUserId(userId);
        List<TaskCheck> checks = checkRepo.findByUserIdAndCheckDate(userId, date);

        // Count total tasks across all topics
        int totalTasks = 0;
        List<SummaryResponse.TopicSummary> breakdown = new ArrayList<>();

        for (StudyTopic topic : topics) {
            int topicTotal = countSubTasks(topic.getSubTasks());
            if (topicTotal == 0) continue;

            long topicChecked = checks.stream()
                    .filter(c -> c.getTopicId().equals(topic.getId()))
                    .count();

            breakdown.add(new SummaryResponse.TopicSummary(topic.getName(), (int) topicChecked, topicTotal));
            totalTasks += topicTotal;
        }

        int completedTasks = checks.size();
        int missedTasks = Math.max(0, totalTasks - completedTasks);
        double completionRate = totalTasks == 0 ? 0.0 : (double) completedTasks / totalTasks;

        // Score calculation
        int baseScore    = completedTasks * 10;
        int penalty      = missedTasks * 5;
        int streakBonus  = completionRate >= 0.7 ? stats.getCurrentStreak() * 5 : 0;
        int perfectBonus = completionRate == 1.0 && totalTasks > 0 ? 20 : 0;
        int dailyScore   = Math.max(0, baseScore - penalty + streakBonus + perfectBonus);

        // Streak update
        int streakChange;
        if (completionRate >= 0.7 && totalTasks > 0) {
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            stats.setBestStreak(Math.max(stats.getBestStreak(), stats.getCurrentStreak()));
            streakChange = 1;
        } else {
            stats.setCurrentStreak(0);
            streakChange = 0;
        }

        stats.setTotalScore(stats.getTotalScore() + dailyScore);
        stats.setLevel(calculateLevel(stats.getTotalScore()));

        SummaryResponse summary = new SummaryResponse();
        summary.setDate(date);
        summary.setCompletedTasks(completedTasks);
        summary.setTotalTasks(totalTasks);
        summary.setCompletionRate(completionRate);
        summary.setDailyScore(dailyScore);
        summary.setStreakChange(streakChange);
        summary.setCurrentStreak(stats.getCurrentStreak());
        summary.setBreakdown(breakdown);
        return summary;
    }

    // ── LEVEL SYSTEM ──────────────────────────────────────────────────────────

    private String calculateLevel(int score) {
        if (score >= 2000) return "Champion";
        if (score >= 1000) return "Expert";
        if (score >= 600)  return "Dedicated";
        if (score >= 300)  return "Focused";
        if (score >= 100)  return "Scholar";
        return "Seedling";
    }

    private StatsResponse toStatsResponse(UserStats stats) {
        StatsResponse r = new StatsResponse();
        r.setTotalScore(stats.getTotalScore());
        r.setCurrentStreak(stats.getCurrentStreak());
        r.setBestStreak(stats.getBestStreak());
        r.setLevel(stats.getLevel());
        r.setLevelEmoji(levelEmoji(stats.getLevel()));
        r.setPointsToNextLevel(pointsToNextLevel(stats.getTotalScore()));
        return r;
    }

    private String levelEmoji(String level) {
        return switch (level) {
            case "Scholar"   -> "📖";
            case "Focused"   -> "⚡";
            case "Dedicated" -> "🔥";
            case "Expert"    -> "💎";
            case "Champion"  -> "🏆";
            default          -> "🌱";
        };
    }

    private int pointsToNextLevel(int score) {
        if (score < 100)  return 100 - score;
        if (score < 300)  return 300 - score;
        if (score < 600)  return 600 - score;
        if (score < 1000) return 1000 - score;
        if (score < 2000) return 2000 - score;
        return 0;
    }

    private int countSubTasks(String subTasksJson) {
        if (subTasksJson == null || subTasksJson.isBlank()) return 0;
        // Count commas in JSON array as a lightweight approach
        String trimmed = subTasksJson.trim();
        if (trimmed.equals("[]") || trimmed.isEmpty()) return 0;
        return (int) trimmed.chars().filter(c -> c == '"').count() / 2;
    }

    private UserStats getOrCreateStats(Long userId) {
        return statsRepo.findByUserId(userId).orElseGet(() -> {
            UserStats s = new UserStats(userId);
            s.setLevel("Seedling");
            return statsRepo.save(s);
        });
    }
}
