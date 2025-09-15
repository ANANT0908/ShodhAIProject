package com.shodhacode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shodhacode.model.Contest;
import com.shodhacode.model.Submission;
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

    @GetMapping("/contests/{contestId}")
    public ResponseEntity<Contest> getContest(@PathVariable("contestId") String contestId) {
        return contestRepository.findById(contestId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@PathVariable("contestId") String contestId) {
        if (!contestRepository.existsById(contestId)) {
            return ResponseEntity.notFound().build();
        }

        // Fetch all submissions and filter by contest ID via problem.contest.id
        List<Submission> submissions = submissionRepository.findAll().stream()
                .filter(s -> s.getProblem() != null
                        && s.getProblem().getContest() != null
                        && Objects.equals(s.getProblem().getContest().getId(), contestId))
                .collect(Collectors.toList());

        // Aggregate scores by username
        Map<String, Integer> scores = new HashMap<>();
        for (Submission s : submissions) {
            String username = s.getUsername();
            Integer sc = (s.getScore() != null) ? s.getScore() : 0;
            scores.merge(username, sc, Integer::sum);
        }

        // Sort leaderboard by score (desc)
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
}
