package com.shodhacode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shodhacode.model.Submission;
import com.shodhacode.model.User;
import com.shodhacode.repository.SubmissionRepository;
import com.shodhacode.repository.UserRepository;
import com.shodhacode.service.SubmissionService;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new submission
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSubmission(@RequestBody Map<String, Object> body) {
        try {
            Long problemId = Long.valueOf(body.get("problemId").toString());
            String language = body.get("language").toString();
            String sourceCode = body.get("sourceCode").toString();
            String username = body.get("username").toString();

            // Find or create user
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.save(new User(username)));

            Submission submission = submissionService.enqueueSubmission(problemId, user, language, sourceCode);

            Map<String, Object> response = new HashMap<>();
            response.put("submissionId", submission.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get submission status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmission(@PathVariable Long id) {
        return submissionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
