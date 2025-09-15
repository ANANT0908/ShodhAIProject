package com.shodhacode.controller;

import com.shodhacode.dto.SubmitRequest;
import com.shodhacode.model.Submission;
import com.shodhacode.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody SubmitRequest req) {
        String uuid = submissionService.enqueueSubmission(req);
        return ResponseEntity.ok(Map.of("submissionId", uuid));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<?> getStatus(@PathVariable String submissionId) {
        Optional<Submission> opt = submissionService.findByUuid(submissionId);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }
}
