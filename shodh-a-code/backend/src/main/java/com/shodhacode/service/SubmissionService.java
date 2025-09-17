// File: backend/src/main/java/com/shodhacode/service/SubmissionService.java
package com.shodhacode.service;

import com.shodhacode.model.Problem;
import com.shodhacode.model.Submission;
import com.shodhacode.model.User;
import com.shodhacode.repository.ProblemRepository;
import com.shodhacode.repository.SubmissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private JudgeService judgeService;

    private final BlockingQueue<Submission> queue = new LinkedBlockingQueue<>();
    private ExecutorService judgePool;
    private Thread consumerThread;

    @PostConstruct
    public void init() {
        judgePool = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "judge-worker-" + count.getAndIncrement());
            }
        });

        consumerThread = new Thread(this::consumeLoop, "submission-consumer");
        consumerThread.start();
    }

    /**
     * Enqueue a submission for judging.
     */
    public Submission enqueueSubmission(Long problemId, User user, String language, String sourceCode) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setLanguage(language);   // ✅ trust client-provided language
        submission.setSourceCode(sourceCode);
        submission.setStatus("Pending");

        Submission saved = submissionRepository.save(submission);
        queue.offer(saved);
        return saved;
    }

    private void consumeLoop() {
        while (true) {
            try {
                Submission submission = queue.take();
                judgePool.submit(() -> processSubmission(submission));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processSubmission(Submission s) {
        try {
            s.setStatus("Running");
            submissionRepository.save(s);

            var problem = s.getProblem();

            // 🔥 Normalize escaped characters from JSON payload
            String normalizedSource = s.getSourceCode()
                    .replace("\\r\\n", "\n")
                    .replace("\\n", "\n")
                    .replaceAll("\r", "");
            s.setSourceCode(normalizedSource);

            // ✅ Correct parameter order (language, sourceCode, submissionUuid, testCases)
            var result = judgeService.runSubmissionInDocker(
                    s.getLanguage(),
                    normalizedSource,
                    String.valueOf(s.getId()),
                    problem.getTestCases()
            );

            s.setStatus(result.getStatus());
            s.setScore("Accepted".equalsIgnoreCase(result.getStatus()) ? 100 : 0);

            submissionRepository.save(s);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ log for debugging
            s.setStatus("Error");
            s.setScore(0);
            submissionRepository.save(s);
        }
    }
}
