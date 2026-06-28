package com.aicare.service;

import com.aicare.dto.DailyTasksRequest;
import com.aicare.dto.DailyTasksResponse;
import com.aicare.dto.LearningRequest;
import com.aicare.dto.LearningResponse;
import com.aicare.dto.QuizRequest;
import com.aicare.dto.QuizQuestion;
import com.aicare.dto.QuizResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class LearningService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public LearningResponse explain(LearningRequest req) {
        String topic      = req.getTopic();
        String task       = req.getTask();
        int    day        = Math.max(1, req.getDayNumber());
        int    totalDays  = req.getTotalDays() > 0 ? req.getTotalDays() : 30;
        double dailyHours = req.getDailyHours() > 0 ? req.getDailyHours() : 1.5;

        // ── Phase calculation ──────────────────────────────────────────────────
        // Foundation: first 20%  |  Intermediate: 20–50%
        // Advanced:   50–80%     |  Pro: last 20%
        double pct = (double) day / totalDays;
        String phase;
        int phaseStart, phaseEnd;

        if (pct <= 0.20) {
            phase = "Foundation";
            phaseStart = 1;
            phaseEnd   = (int) Math.ceil(totalDays * 0.20);
        } else if (pct <= 0.50) {
            phase = "Intermediate";
            phaseStart = (int) Math.ceil(totalDays * 0.20) + 1;
            phaseEnd   = (int) Math.ceil(totalDays * 0.50);
        } else if (pct <= 0.80) {
            phase = "Advanced";
            phaseStart = (int) Math.ceil(totalDays * 0.50) + 1;
            phaseEnd   = (int) Math.ceil(totalDays * 0.80);
        } else {
            phase = "Pro";
            phaseStart = (int) Math.ceil(totalDays * 0.80) + 1;
            phaseEnd   = totalDays;
        }

        int totalPercent = (int) Math.round(pct * 100);
        int phasePercent = phaseEnd > phaseStart
            ? (int) Math.round((double)(day - phaseStart) / (phaseEnd - phaseStart) * 100)
            : 100;
        phasePercent = Math.max(0, Math.min(100, phasePercent));

        LearningResponse resp;

        boolean hasKey = geminiApiKey != null && !geminiApiKey.isBlank()
                      && !geminiApiKey.equals("YOUR_GEMINI_API_KEY");

        if (hasKey) {
            try {
                resp = callGemini(topic, task, day, totalDays, dailyHours, phase);
            } catch (Exception e) {
                resp = fallback(topic, task, day, totalDays, dailyHours, phase);
            }
        } else {
            resp = fallback(topic, task, day, totalDays, dailyHours, phase);
        }

        resp.setDayNumber(day);
        resp.setTotalDays(totalDays);
        resp.setPhase(phase);
        resp.setPhasePercent(phasePercent);
        resp.setTotalPercent(totalPercent);
        return resp;
    }

    // ── GEMINI API ────────────────────────────────────────────────────────────

    private LearningResponse callGemini(String topic, String task,
            int day, int total, double hours, String phase) throws Exception {

        String prompt = buildPrompt(topic, task, day, total, hours, phase);

        String body = """
            {
              "contents": [{"parts": [{"text": %s}]}],
              "generationConfig": {"responseMimeType": "application/json"}
            }
            """.formatted(toJsonString(prompt));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_URL + geminiApiKey))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(res.body());
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        JsonNode parsed = mapper.readTree(text);

        LearningResponse r = new LearningResponse();
        r.setTopic(topic);
        r.setTask(task);
        r.setExplanation(parsed.path("explanation").asText());
        r.setKeyConcepts(toList(parsed.path("keyConcepts")));
        r.setStudySteps(toList(parsed.path("studySteps")));
        r.setExercises(toList(parsed.path("exercises")));
        r.setYoutubeSearchTerms(toList(parsed.path("youtubeSearchTerms")));
        r.setNextPreview(parsed.path("nextPreview").asText(""));
        r.setAiGenerated(true);
        return r;
    }

    private String buildPrompt(String topic, String task,
            int day, int total, double hours, String phase) {

        int p1 = (int) Math.ceil(total * 0.20);
        int p2 = (int) Math.ceil(total * 0.50);
        int p3 = (int) Math.ceil(total * 0.80);

        return """
You are an expert curriculum designer and educator for "%s".

STUDENT CONTEXT:
- Topic: %s
- Today's task: %s
- Day %d of %d  (%s phase)
- Study time: %.1f hours today
- Curriculum phases:
    Foundation    → Days 1–%d   (what, why, first steps)
    Intermediate  → Days %d–%d  (core features, hands-on)
    Advanced      → Days %d–%d  (deep dives, architecture)
    Pro           → Days %d–%d  (expert patterns, projects, certs)

Generate content for DAY %d that is strictly appropriate for the %s phase.
Content must progress naturally from what came before and prepare for what comes next.
YouTube search terms must reflect the exact skill level (add "beginner", "intermediate", "advanced", or "pro" accordingly).

Respond ONLY with valid JSON — no markdown, no extra text:
{
  "explanation": "2–3 paragraph explanation suited to day %d / %s phase learner",
  "keyConcepts": ["concept 1", "concept 2", "concept 3", "concept 4", "concept 5"],
  "studySteps": [
    "Step 1 (%.0f-min task): ...",
    "Step 2 (%.0f-min task): ...",
    "Step 3 (%.0f-min task): ...",
    "Step 4 (hands-on): ...",
    "Step 5 (review/test): ..."
  ],
  "exercises": [
    "Exercise 1 (%s level): ...",
    "Exercise 2 (hands-on): ...",
    "Exercise 3 (challenge): ..."
  ],
  "youtubeSearchTerms": [
    "%s %s %s tutorial",
    "%s %s day %d hands on",
    "%s %s %s course",
    "%s %s %s guide",
    "%s %s from scratch to %s"
  ],
  "nextPreview": "Tomorrow (Day %d) you will cover: ..."
}
""".formatted(
    topic, topic, task, day, total, phase, hours,
    p1, p1+1, p2, p2+1, p3, p3+1, total,
    day, phase,
    day, phase,
    hours * 60 * 0.20, hours * 60 * 0.30, hours * 60 * 0.30,
    phase,
    topic, task, phase.toLowerCase(),
    topic, task, day,
    topic, task, phase.toLowerCase(),
    topic, task, phase.toLowerCase(),
    topic, task, phase.equalsIgnoreCase("Foundation") ? "intermediate" : "pro",
    day + 1
);
    }

    // ── FALLBACK (no Gemini key) ──────────────────────────────────────────────

    private LearningResponse fallback(String topic, String task,
            int day, int total, double hours, String phase) {

        String level = switch (phase) {
            case "Foundation"    -> "beginner";
            case "Intermediate"  -> "intermediate";
            case "Advanced"      -> "advanced";
            default              -> "expert";
        };

        String phaseContext = switch (phase) {
            case "Foundation"   -> "You are just starting out. Focus on understanding the core idea of " + task + " and why it exists. Don't worry about mastering everything — just build a solid mental model.";
            case "Intermediate" -> "You now have the basics. Today's task dives into the practical side of " + task + ". Focus on hands-on work and connecting theory to real usage in " + topic + ".";
            case "Advanced"     -> "You're building real depth. Today's focus on " + task + " will push you into architectural thinking and edge cases. Start thinking about how this fits into production systems.";
            default             -> "You're at expert level. Today's work on " + task + " should result in something you could put in a portfolio or use in a real project. Think certification-level understanding.";
        };

        int hoursMin = (int) Math.round(hours * 60);
        int seg = hoursMin / 4;

        LearningResponse r = new LearningResponse();
        r.setTopic(topic);
        r.setTask(task);
        r.setAiGenerated(false);

        r.setExplanation(
            "Day " + day + " of " + total + " — " + phase + " Phase\n\n" +
            phaseContext + "\n\n" +
            "With " + hours + "h today, follow the study steps below carefully. " +
            "Use the YouTube search buttons to find a " + level + "-level video that suits your style. " +
            "Take notes as you go and complete the exercises before moving on."
        );

        r.setKeyConcepts(switch (phase) {
            case "Foundation" -> List.of(
                "What is " + task + " and why does it exist?",
                "Core terminology and vocabulary for " + task,
                "How " + task + " fits into the bigger " + topic + " picture",
                "Basic setup or configuration needed",
                "The most common beginner use case for " + task
            );
            case "Intermediate" -> List.of(
                "Key configuration options and parameters for " + task,
                "Integration of " + task + " with other " + topic + " components",
                "Performance and cost considerations",
                "Security best practices for " + task,
                "Troubleshooting common " + task + " issues"
            );
            case "Advanced" -> List.of(
                "Production architecture patterns using " + task,
                "Scaling and high-availability design with " + task,
                "Advanced security hardening for " + task,
                "Monitoring, logging, and observability",
                "Cost optimization strategies for " + task + " at scale"
            );
            default -> List.of(
                "Expert-level " + task + " design patterns",
                "Real-world case studies and solutions",
                "Certification-level understanding of " + task,
                "Pitfalls that trip up senior engineers",
                "How to explain " + task + " in a technical interview"
            );
        });

        r.setStudySteps(List.of(
            "Step 1 (" + seg + " min): Search YouTube for \"" + topic + " " + task + " " + level + "\" and watch an intro video",
            "Step 2 (" + seg + " min): Read the official documentation or a top article about " + task,
            "Step 3 (" + seg + " min): Write the key concepts in your own words in the Notes section",
            "Step 4 (" + seg + " min): Do a hands-on exercise — build, configure, or code something with " + task,
            "Step 5 (5 min): Self-quiz — close your notes and list 3 things you learned today"
        ));

        r.setExercises(switch (phase) {
            case "Foundation" -> List.of(
                "Beginner: Explain what " + task + " is in 2 sentences without jargon",
                "Beginner: List 3 real-world scenarios where " + task + " is used in " + topic,
                "Beginner: Set up or access " + task + " for the first time and take a screenshot"
            );
            case "Intermediate" -> List.of(
                "Intermediate: Configure " + task + " for a small project and document each step",
                "Intermediate: Compare " + task + " with an alternative — list pros and cons",
                "Intermediate: Find a bug or issue with " + task + " online and explain the solution"
            );
            case "Advanced" -> List.of(
                "Advanced: Design a production architecture that uses " + task + " with high availability",
                "Advanced: Write a runbook for deploying and monitoring " + task + " in production",
                "Advanced: Identify 3 security risks in a typical " + task + " setup and fix them"
            );
            default -> List.of(
                "Pro: Build a complete working example using " + task + " suitable for your portfolio",
                "Pro: Answer this interview question: \"Explain how you would architect " + task + " at scale\"",
                "Pro: Review the " + topic + " certification guide for " + task + " topics and test yourself"
            );
        });

        r.setYoutubeSearchTerms(List.of(
            topic + " " + task + " " + level + " tutorial",
            topic + " " + task + " " + level + " explained",
            topic + " " + task + " hands on " + level,
            topic + " day " + day + " " + task + " course",
            topic + " " + task + " " + (day <= total / 2 ? "getting started" : "deep dive")
        ));

        r.setNextPreview("Day " + (day + 1) + ": Continue building on " + task +
            " by exploring related " + phase + "-level " + topic + " concepts.");

        return r;
    }

    // ── DAILY TASKS GENERATION ────────────────────────────────────────────────

    public DailyTasksResponse generateDailyTasks(DailyTasksRequest req) {
        String topic     = req.getTopic();
        int    day       = Math.max(1, req.getDayNumber());
        int    total     = req.getTotalDays() > 0 ? req.getTotalDays() : 30;
        double hours     = req.getDailyHours() > 0 ? req.getDailyHours() : 1.5;
        int    numTasks  = req.getNumTasks() > 0 ? req.getNumTasks() : 4;

        double pct = (double) day / total;
        String phase = pct <= 0.20 ? "Foundation"
                     : pct <= 0.50 ? "Intermediate"
                     : pct <= 0.80 ? "Advanced" : "Pro";

        DailyTasksResponse resp;
        boolean hasKey = geminiApiKey != null && !geminiApiKey.isBlank()
                      && !geminiApiKey.equals("YOUR_GEMINI_API_KEY");

        if (hasKey) {
            try {
                resp = callGeminiForTasks(topic, day, total, hours, numTasks, phase);
            } catch (Exception e) {
                resp = fallbackTasks(topic, day, total, hours, numTasks, phase);
            }
        } else {
            resp = fallbackTasks(topic, day, total, hours, numTasks, phase);
        }
        resp.setDayNumber(day);
        resp.setPhase(phase);
        return resp;
    }

    private DailyTasksResponse callGeminiForTasks(String topic, int day, int total,
            double hours, int numTasks, String phase) throws Exception {

        String prompt = """
You are a curriculum designer creating a %d-day learning plan for "%s".
Today is Day %d of %d (%s phase, %.1f hours/day).

Generate EXACTLY %d specific, actionable tasks for Day %d that:
1. Are unique to this specific day (different from all other days)
2. Logically progress from the previous day's topics
3. Are completable within %.1f hours
4. Match the %s skill level
5. Cover a specific sub-topic or concept the student should learn TODAY

Think of it as a structured textbook — each day covers a distinct chapter or concept.

Respond ONLY with JSON, no markdown:
{
  "dailyFocus": "one-line theme for today (e.g. 'AWS IAM & Security Fundamentals')",
  "tasks": [
    "Task 1: specific action or concept",
    "Task 2: specific action or concept",
    "Task 3: specific action or concept",
    "Task 4: specific action or concept"
  ]
}
""".formatted(total, topic, day, total, phase, hours, numTasks, day, hours, phase);

        String body = """
            {"contents":[{"parts":[{"text":%s}]}],"generationConfig":{"responseMimeType":"application/json"}}
            """.formatted(toJsonString(prompt));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_URL + geminiApiKey))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(res.body());
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        JsonNode parsed = mapper.readTree(text);

        DailyTasksResponse r = new DailyTasksResponse();
        r.setDailyFocus(parsed.path("dailyFocus").asText("Day " + day + " — " + phase + " Phase"));
        r.setTasks(toList(parsed.path("tasks")));
        return r;
    }

    private DailyTasksResponse fallbackTasks(String topic, int day, int total,
            double hours, int numTasks, String phase) {

        // ── Phase boundaries ──────────────────────────────────────────────────
        int p1End = (int) Math.ceil(total * 0.20);   // end of Foundation
        int p2End = (int) Math.ceil(total * 0.50);   // end of Intermediate
        int p3End = (int) Math.ceil(total * 0.80);   // end of Advanced

        // Day index within the current phase (0-based)
        int phaseDay = switch (phase) {
            case "Intermediate" -> day - p1End - 1;
            case "Advanced"     -> day - p2End - 1;
            case "Pro"          -> day - p3End - 1;
            default             -> day - 1;
        };

        // Curriculum blueprints — cycle through specific topics per phase
        List<String[]> blueprint = switch (phase) {
            case "Foundation" -> List.of(
                new String[]{"Introduction to " + topic, "What is " + topic + " and why it matters", "History and background of " + topic, "Core use cases of " + topic},
                new String[]{"Setting up your environment", "Install required tools for " + topic, "Create your first " + topic + " account/project", "Explore the main dashboard or interface"},
                new String[]{"Core concepts Part 1", "Fundamental terminology in " + topic, "Key components overview", "How they interact with each other"},
                new String[]{"Core concepts Part 2", "Data flow and architecture basics", "Read the official " + topic + " getting started guide", "Take notes on the main concepts"},
                new String[]{"First hands-on task", "Follow an official " + topic + " tutorial", "Build a simple 'Hello World' equivalent", "Verify your setup works end to end"},
                new String[]{"Basic operations", "Create your first resource in " + topic, "Edit and update it", "Delete and clean up safely"},
                new String[]{"Common beginner patterns", "Most used features in " + topic, "Explore examples from the official docs", "Replicate one example on your own"},
                new String[]{"Foundation review", "Summarize what you learned this week", "Quiz yourself on core terminology", "Identify 3 things you're unsure about and look them up"}
            );
            case "Intermediate" -> List.of(
                new String[]{"Core feature deep dive: Part 1", "Study the most important " + topic + " service/module", "Configure it with a real use case", "Test and verify it works"},
                new String[]{"Core feature deep dive: Part 2", "Advanced configuration options", "Common patterns and anti-patterns", "Hands-on lab: configure for a real scenario"},
                new String[]{"Integration and connectivity", "How " + topic + " connects to other tools", "Set up an integration pipeline", "Test the data or control flow"},
                new String[]{"Security fundamentals", "Authentication in " + topic, "Authorization and permissions", "Apply least-privilege principle to your setup"},
                new String[]{"Error handling and debugging", "Common errors in " + topic + " and their fixes", "Read logs and error messages", "Practice fixing a broken configuration"},
                new String[]{"Performance basics", "How to measure performance in " + topic, "Identify bottlenecks", "Apply basic optimizations"},
                new String[]{"Real-world mini project", "Define a small project using " + topic, "Implement it step by step", "Document what you built"},
                new String[]{"Intermediate review", "Review all intermediate topics covered", "Take an online quiz or practice test", "Identify gaps and fill them"}
            );
            case "Advanced" -> List.of(
                new String[]{"Production architecture patterns", "High-availability design with " + topic, "Fault tolerance strategies", "Document a multi-region or multi-AZ setup"},
                new String[]{"Advanced security", "Encryption at rest and in transit for " + topic, "Audit logs and compliance basics", "Security hardening checklist"},
                new String[]{"Scaling and load management", "Horizontal vs vertical scaling in " + topic, "Auto-scaling configuration", "Load testing and results analysis"},
                new String[]{"Monitoring and observability", "Set up metrics and alerts for " + topic, "Explore logs and traces", "Create a monitoring dashboard"},
                new String[]{"Cost optimization", "Understand pricing model for " + topic, "Identify cost waste in your setup", "Apply cost-saving recommendations"},
                new String[]{"Advanced patterns", "Design patterns used by experts in " + topic, "Study a real-world case study", "Replicate the pattern in your environment"},
                new String[]{"CI/CD and automation", "Automate " + topic + " deployment", "Infrastructure as code basics", "Set up a simple automated pipeline"},
                new String[]{"Advanced review + challenge", "Build something non-trivial with " + topic, "Code review or architecture review", "Document lessons learned"}
            );
            default -> List.of(  // Pro
                new String[]{"Expert-level design", "Study an enterprise-grade " + topic + " architecture", "Identify trade-offs and decisions made", "Write your own architecture document"},
                new String[]{"Certification preparation", "Study " + topic + " certification guide (if available)", "Take a practice exam", "Review weak areas"},
                new String[]{"Real project: Planning", "Define a portfolio-worthy " + topic + " project", "Break it into milestones", "Set up the project scaffold"},
                new String[]{"Real project: Implementation", "Build core functionality", "Add error handling and edge cases", "Commit code with clear documentation"},
                new String[]{"Real project: Testing and hardening", "Write tests for your project", "Security review", "Performance optimization"},
                new String[]{"Real project: Deployment", "Deploy your project to a real environment", "Set up monitoring", "Create a README or documentation"},
                new String[]{"Knowledge sharing", "Write a blog post or notes about what you built", "Prepare to explain it in an interview", "Identify what to build next"},
                new String[]{"Final mastery review", "Complete a full " + topic + " review from scratch to pro", "Test yourself without notes", "Celebrate your progress! 🎉"}
            );
        };

        // Cycle through blueprint entries for the current phase
        String[] todayTasks = blueprint.get(phaseDay % blueprint.size());

        // Build response
        DailyTasksResponse r = new DailyTasksResponse();
        r.setDailyFocus("Day " + day + " — " + todayTasks[0]);

        List<String> taskList = new ArrayList<>();
        // Take up to numTasks from the blueprint
        for (int i = 0; i < Math.min(numTasks, todayTasks.length); i++) {
            taskList.add(todayTasks[i]);
        }
        // If more tasks needed, add study-time based extras
        while (taskList.size() < numTasks) {
            taskList.add("Review today's topic and add to your notes");
        }
        r.setTasks(taskList);
        return r;
    }

    // ── QUIZ GENERATION ──────────────────────────────────────────────────────

    public QuizResponse generateQuiz(QuizRequest req) {
        String topic    = req.getTopic();
        int    day      = Math.max(1, req.getDayNumber());
        int    total    = req.getTotalDays() > 0 ? req.getTotalDays() : 30;
        double hours    = req.getDailyHours() > 0 ? req.getDailyHours() : 1.5;

        double pct = (double) day / total;
        String phase = pct <= 0.20 ? "Foundation"
                     : pct <= 0.50 ? "Intermediate"
                     : pct <= 0.80 ? "Advanced" : "Pro";

        boolean hasKey = geminiApiKey != null && !geminiApiKey.isBlank()
                      && !geminiApiKey.equals("YOUR_GEMINI_API_KEY");

        List<QuizQuestion> questions;
        try {
            questions = hasKey
                ? callGeminiForQuiz(topic, day, total, phase)
                : fallbackQuiz(topic, day, phase);
        } catch (Exception e) {
            questions = fallbackQuiz(topic, day, phase);
        }

        QuizResponse resp = new QuizResponse();
        resp.setQuestions(questions);
        resp.setPhase(phase);
        resp.setDayNumber(day);
        return resp;
    }

    private List<QuizQuestion> callGeminiForQuiz(String topic, int day, int total, String phase) throws Exception {
        String prompt = """
You are a quiz generator for a student learning "%s" on Day %d of %d (%s phase).

Generate EXACTLY 5 multiple-choice questions that test understanding of %s concepts appropriate for the %s phase.
Questions must be clear, unambiguous, and have exactly one correct answer.

Respond ONLY with valid JSON, no markdown:
{
  "questions": [
    {
      "question": "Question text here?",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Brief explanation why the answer is correct"
    }
  ]
}
""".formatted(topic, day, total, phase, topic, phase);

        String body = """
            {"contents":[{"parts":[{"text":%s}]}],"generationConfig":{"responseMimeType":"application/json"}}
            """.formatted(toJsonString(prompt));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_URL + geminiApiKey))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(res.body());
        String text = root.at("/candidates/0/content/parts/0/text").asText();
        JsonNode parsed = mapper.readTree(text);

        List<QuizQuestion> list = new ArrayList<>();
        for (JsonNode q : parsed.path("questions")) {
            List<String> opts = new ArrayList<>();
            q.path("options").forEach(o -> opts.add(o.asText()));
            list.add(new QuizQuestion(
                q.path("question").asText(),
                opts,
                q.path("correctIndex").asInt(0),
                q.path("explanation").asText("")
            ));
        }
        return list;
    }

    private List<QuizQuestion> fallbackQuiz(String topic, int day, String phase) {
        return switch (phase) {
            case "Foundation" -> List.of(
                new QuizQuestion("What is the primary purpose of " + topic + "?",
                    List.of("To replace all existing tools", "To provide a managed, scalable solution for common computing needs", "To only work with legacy systems", "To eliminate the need for software developers"),
                    1, topic + " is designed to provide scalable and managed solutions for modern computing needs."),
                new QuizQuestion("Which of the following best describes a key benefit of using " + topic + "?",
                    List.of("Increased hardware costs", "Reduced scalability", "Faster deployment and managed infrastructure", "Requires more manual configuration than alternatives"),
                    2, "One of the main advantages of " + topic + " is the ability to deploy faster with managed infrastructure."),
                new QuizQuestion("In the context of " + topic + ", what does 'provisioning' mean?",
                    List.of("Deleting unused resources", "Setting up and allocating resources needed to run services", "Monitoring performance metrics", "Writing application code"),
                    1, "Provisioning refers to the setup and allocation of resources required for services in " + topic + "."),
                new QuizQuestion("Which step comes first when getting started with " + topic + "?",
                    List.of("Writing production code immediately", "Setting up your account and environment", "Optimizing for cost", "Deploying to multiple regions"),
                    1, "The first step is always to set up your account and environment before doing anything else."),
                new QuizQuestion("What is a best practice when learning " + topic + " as a beginner?",
                    List.of("Skip documentation and learn by trial and error only", "Follow official tutorials and use the free tier", "Deploy directly to production to learn faster", "Avoid using the official documentation"),
                    1, "Following official tutorials and using the free tier is the safest and most effective way to learn " + topic + ".")
            );
            case "Intermediate" -> List.of(
                new QuizQuestion("What is the principle of least privilege in " + topic + "?",
                    List.of("Give all users admin access for convenience", "Grant only the minimum permissions needed to perform a task", "Share credentials across team members", "Disable all access controls"),
                    1, "Least privilege means granting only the minimum permissions required, reducing security risk."),
                new QuizQuestion("Which approach is recommended for handling configuration in " + topic + "?",
                    List.of("Hardcode all values directly in the application", "Use environment variables or a configuration service", "Store credentials in version control", "Use the same config for dev and production"),
                    1, "Using environment variables or a configuration service keeps secrets out of code and makes deployments flexible."),
                new QuizQuestion("What does horizontal scaling mean in " + topic + "?",
                    List.of("Upgrading to a bigger single machine", "Adding more instances of the same service", "Reducing the number of servers", "Increasing the disk size of one machine"),
                    1, "Horizontal scaling means adding more instances (machines) rather than making one machine bigger."),
                new QuizQuestion("When debugging an issue in " + topic + ", what should you check first?",
                    List.of("Delete and recreate everything", "Restart without investigating", "Check logs and error messages for specific details", "Assume it's a network issue"),
                    2, "Logs and error messages contain the most direct information about what went wrong."),
                new QuizQuestion("What is the benefit of infrastructure as code (IaC) with " + topic + "?",
                    List.of("Makes infrastructure harder to reproduce", "Allows manual-only deployments", "Enables repeatable, version-controlled infrastructure deployment", "Increases deployment time"),
                    2, "IaC makes deployments reproducible, version-controlled, and consistent across environments.")
            );
            case "Advanced" -> List.of(
                new QuizQuestion("What is the primary purpose of a load balancer in a " + topic + " architecture?",
                    List.of("To store data permanently", "To distribute incoming traffic across multiple instances", "To replace a database", "To encrypt all network traffic"),
                    1, "A load balancer distributes traffic across multiple instances to prevent any one instance from being overwhelmed."),
                new QuizQuestion("Which strategy best ensures high availability in " + topic + "?",
                    List.of("Running everything on a single server", "Deploying across multiple availability zones", "Using only one region", "Disabling auto-scaling"),
                    1, "Deploying across multiple availability zones ensures the system stays up even if one zone fails."),
                new QuizQuestion("What is observability in the context of " + topic + "?",
                    List.of("Making the code open source", "The ability to measure and understand internal system state through metrics, logs, and traces", "Using only error logs", "A type of database"),
                    1, "Observability combines metrics, logs, and traces to give visibility into how a system is behaving."),
                new QuizQuestion("Why is cost optimization important when running " + topic + " at scale?",
                    List.of("It reduces system reliability", "Cloud costs grow significantly at scale; optimization ensures efficiency", "It's only relevant for startups", "It makes the system slower"),
                    1, "At scale, small inefficiencies become large costs — optimization is critical for sustainable operations."),
                new QuizQuestion("What does a Circuit Breaker pattern do in distributed systems using " + topic + "?",
                    List.of("Physically cuts power to servers", "Stops sending requests to a failing service to prevent cascading failures", "Increases request timeouts indefinitely", "Duplicates all API calls"),
                    1, "A circuit breaker detects failures and stops sending requests to prevent cascading failures across services.")
            );
            default -> List.of( // Pro
                new QuizQuestion("What is the CAP theorem and how does it apply to " + topic + "?",
                    List.of("A theory about computer hardware capacity", "The impossibility of simultaneously guaranteeing Consistency, Availability, and Partition tolerance", "A cloud cost calculation method", "A security framework"),
                    1, "CAP theorem states distributed systems can only guarantee 2 of 3: Consistency, Availability, Partition tolerance."),
                new QuizQuestion("In a microservices architecture using " + topic + ", what is service mesh used for?",
                    List.of("Storing user data", "Managing service-to-service communication, security, and observability", "Replacing the database layer", "Compiling application code"),
                    1, "A service mesh handles service-to-service communication, providing load balancing, security, and observability."),
                new QuizQuestion("What is blue-green deployment in " + topic + "?",
                    List.of("A color scheme for dashboards", "Running two identical environments and switching traffic to the new one after testing", "A type of database migration", "A network security protocol"),
                    1, "Blue-green deployment uses two identical environments — traffic switches to the new one after validation, enabling zero-downtime deploys."),
                new QuizQuestion("What is eventual consistency in distributed " + topic + " systems?",
                    List.of("Data is always consistent immediately after any write", "All replicas will converge to the same value given enough time without new updates", "A database locking mechanism", "A caching strategy"),
                    1, "Eventual consistency guarantees that, given no new updates, all replicas will eventually reach the same value."),
                new QuizQuestion("Which pattern is best for handling cross-cutting concerns like auth and logging in " + topic + "?",
                    List.of("Copy-paste the logic into every service", "Use an API gateway or middleware layer", "Disable logging in production", "Handle each concern in the database"),
                    1, "An API gateway centralizes cross-cutting concerns like authentication, rate limiting, and logging.")
            );
        };
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> list.add(n.asText()));
        return list;
    }

    private String toJsonString(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "") + "\"";
    }
}
