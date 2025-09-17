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

    // existing endpoint
    @GetMapping("/contests/{contestId}")
    public ResponseEntity<Contest> getContest(@PathVariable("contestId") String contestId) {
        return contestRepository.findById(contestId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // NEW endpoint: return all problems for a contest
    @GetMapping("/contests/{contestId}/problems")
    public ResponseEntity<List<Problem>> getProblems(@PathVariable("contestId") String contestId) {
        Optional<Contest> contestOpt = contestRepository.findById(contestId);
        if (contestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Contest contest = contestOpt.get();
        // assuming Contest has a getProblems() method returning List<Problem>
        return ResponseEntity.ok(contest.getProblems());
    }

    // existing leaderboard endpoint
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

    @PostMapping("/contests")
        public ResponseEntity<Contest> addContest(@RequestBody Contest contest) {
        // Validate name
        if (contest.getName() == null || contest.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // No need to set ID manually; JPA will generate it
        Contest saved = contestRepository.save(contest);
        return ResponseEntity.ok(saved);
}
}
