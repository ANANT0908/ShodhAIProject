package com.shodhacode.controller;
import com.shodhacode.dto.SubmissionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shodhacode.dto.SubmissionDTO;
import com.shodhacode.model.Submission;
import com.shodhacode.model.User;
import com.shodhacode.repository.SubmissionRepository;
import com.shodhacode.repository.UserRepository;
import com.shodhacode.service.SubmissionService;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> createSubmission(@RequestBody SubmissionRequest body) {
    try {
        User user = userRepository.findByUsername(body.getUsername())
                .orElseGet(() -> userRepository.save(new User(body.getUsername())));

        Submission submission = submissionService.enqueueSubmission(
                body.getProblemId(),
                user,
                body.getLanguage(),
                body.getSourceCode()
        );

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
    public ResponseEntity<?> getSubmission(@PathVariable("id") Long id) {
        try {
            return submissionRepository.findById(id)
                    .map(sub -> {
                        SubmissionDTO dto = new SubmissionDTO();
                        dto.setId(sub.getId());

                        if (sub.getProblem() != null) {
                            dto.setProblemId(sub.getProblem().getId());
                            dto.setProblemTitle(sub.getProblem().getTitle());
                        }

                        if (sub.getUser() != null) {
                            dto.setUserId(sub.getUser().getId());
                            dto.setUsername(sub.getUser().getUsername());
                        }

                        dto.setLanguage(sub.getLanguage());
                        dto.setSourceCode(sub.getSourceCode());
                        dto.setStatus(sub.getStatus());
                        dto.setScore(sub.getScore());
                        dto.setCreatedAt(sub.getCreatedAt());

                        return ResponseEntity.ok(dto);
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
