package com.shodhacode.service;

import com.shodhacode.dto.SubmitRequest;
import com.shodhacode.dto.JudgeResultDto;
import com.shodhacode.model.Problem;
import com.shodhacode.model.Submission;
import com.shodhacode.model.TestCaseEntity;
import com.shodhacode.repository.ProblemRepository;
import com.shodhacode.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class SubmissionService {

    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();
    private final ExecutorService consumerPool = Executors.newSingleThreadExecutor();
    private final ExecutorService judgePool = Executors.newFixedThreadPool(2);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private JudgeService judgeService;

    @PostConstruct
    public void init() {
        consumerPool.submit(() -> {
            while (true) {
                try {
                    Long submissionId = queue.take();
                    processSubmission(submissionId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public String enqueueSubmission(SubmitRequest req) {
        Submission s = new Submission();
        s.setSubmissionUuid(UUID.randomUUID().toString());
        s.setUsername(req.getUsername());
        s.setLanguage(req.getLanguage());
        s.setSourceCode(req.getSourceCode());
        s.setStatus("Pending");
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());

        Optional<Problem> pOpt = problemRepository.findById(req.getProblemId());
        pOpt.ifPresent(s::setProblem);
        Submission saved = submissionRepository.save(s);
        queue.offer(saved.getId());
        return saved.getSubmissionUuid();
    }

    private void processSubmission(Long id) {
        Optional<Submission> opt = submissionRepository.findById(id);
        if (opt.isEmpty()) return;
        Submission s = opt.get();
        s.setStatus("Running");
        s.setUpdatedAt(Instant.now());
        submissionRepository.save(s);

        judgePool.submit(() -> {
            try {
                Problem problem = s.getProblem();
                List<TestCaseEntity> tests = problem.getTestCases();
                JudgeResultDto result = judgeService.runSubmissionInDocker(
                        s.getSubmissionUuid(),
                        s.getLanguage(),
                        s.getSourceCode(),
                        tests
                );
                s.setStatus(result.getStatus());
                s.setResultDetails(result.getDetails());
                if (result.getPassedCount() != null && result.getTotalCount() != null) {
                    s.setScore(result.getPassedCount() * 100 / Math.max(1, result.getTotalCount()));
                }
            } catch (Exception e) {
                s.setStatus("Error");
                s.setResultDetails(e.getMessage());
            } finally {
                s.setUpdatedAt(Instant.now());
                submissionRepository.save(s);
            }
        });
    }

    public Optional<Submission> findByUuid(String uuid) {
        return submissionRepository.findBySubmissionUuid(uuid);
    }
}
