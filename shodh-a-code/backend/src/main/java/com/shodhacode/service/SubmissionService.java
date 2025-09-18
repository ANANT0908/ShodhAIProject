package com.shodhacode.service;

import com.shodhacode.model.Problem;
import com.shodhacode.model.Submission;
import com.shodhacode.model.User;
import com.shodhacode.repository.ProblemRepository;
import com.shodhacode.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.text.StringEscapeUtils;
import jakarta.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

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

    // Enqueue a submission for judging.
     
    public Submission enqueueSubmission(Long problemId, User user, String language, String sourceCode) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setLanguage(language);   
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
                judgePool.submit(() -> processSubmissionTask(submission));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processSubmissionTask(Submission s) {
        try {
            log.info("Processing submission id={}", s.getId());
            s.setStatus("Running");
            submissionRepository.save(s);

            // Re-load problem with testCases initialized in one query.
            Long problemId = s.getProblem() != null ? s.getProblem().getId() : null;
            if (problemId == null) {
                markSubmissionError(s, "Problem reference missing on submission");
                return;
            }

            Problem problemWithTC = problemRepository.findByIdWithTestCases(problemId)
                    .orElseThrow(() -> new RuntimeException("Problem not found id=" + problemId));

            if (problemWithTC.getTestCases() == null || problemWithTC.getTestCases().isEmpty()) {
                // explicit handling - avoid silent behavior when no testcases exist
                markSubmissionError(s, "No test cases defined for problem id=" + problemId);
                return;
            }

            // Normalize escaped characters from JSON payload
            String sourceCode = s.getSourceCode();
            String normalizedSource = (sourceCode == null) ? "" : StringEscapeUtils.unescapeJson(sourceCode);
            s.setSourceCode(normalizedSource);
            submissionRepository.save(s); 

            var result = judgeService.runSubmissionInDocker(
                    s.getLanguage(),
                    normalizedSource,
                    String.valueOf(s.getId()),
                    problemWithTC.getTestCases()
            );


            s.setStatus(result.getStatus());
            s.setScore("Accepted".equalsIgnoreCase(result.getStatus()) ? 100 : 0);

            submissionRepository.save(s);
            log.info("Finished submission id={} -> {}", s.getId(), result.getStatus());
        } catch (Exception e) {
            // log and save an error status
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();

            log.error("❌ Error while processing submission {}: {} \n{}", s != null ? s.getId() : null, e.getMessage(), stack);

            if (s != null) {
                s.setStatus("Error");
                s.setScore(0);
                submissionRepository.save(s);
            }
        }
    }

    private void markSubmissionError(Submission s, String msg) {
        log.warn("Marking submission {} as error: {}", s.getId(), msg);
        s.setStatus("Error");
        s.setScore(0);
        submissionRepository.save(s);
    }
}