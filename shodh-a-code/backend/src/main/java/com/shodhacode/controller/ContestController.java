package com.shodhacode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shodhacode.model.Contest;
import com.shodhacode.model.Submission;
import com.shodhacode.model.Problem;
import com.shodhacode.repository.ContestRepository;
import com.shodhacode.repository.SubmissionRepository;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class ContestController {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @GetMapping("/contests")
    public ResponseEntity<List<Contest>> getAllContests() {
        List<Contest> contests = contestRepository.findAll();
        return ResponseEntity.ok(contests);
    }

    @GetMapping("/contests/{contestId}")
    public ResponseEntity<Contest> getContest(@PathVariable("contestId") Long contestId) {
        return contestRepository.findById(contestId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/contests/{contestId}/problems")
    public ResponseEntity<List<Problem>> getProblems(@PathVariable("contestId") Long contestId) {
        Optional<Contest> contestOpt = contestRepository.findById(contestId);
        if (contestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Contest contest = contestOpt.get();
        return ResponseEntity.ok(contest.getProblems());
    }

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@PathVariable("contestId") Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            return ResponseEntity.notFound().build();
        }

        // Fetch all submissions, filter by contest ID
        List<Submission> submissions = submissionRepository.findAll().stream()
                .filter(s -> s.getProblem() != null
                        && s.getProblem().getContest() != null
                        && Objects.equals(s.getProblem().getContest().getId(), contestId))
                .collect(Collectors.toList());

        // Aggregate scores by user
        Map<String, Integer> scores = new HashMap<>();
        for (Submission s : submissions) {
            String username = (s.getUser() != null) ? s.getUser().getUsername() : "unknown";
            int sc = (s.getScore() != null) ? s.getScore() : 0;
            scores.merge(username, sc, Integer::sum);
        }

        // Sort leaderboard
        List<Map<String, Object>> leaderboard = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("username", entry.getKey());
                    row.put("score", entry.getValue());
                    return row;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }

    @PostMapping("/contests")
    public ResponseEntity<Contest> addContest(@RequestBody Contest contest) {
        if (contest.getName() == null || contest.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Contest saved = contestRepository.save(contest);
        return ResponseEntity.ok(saved);
    }
}
