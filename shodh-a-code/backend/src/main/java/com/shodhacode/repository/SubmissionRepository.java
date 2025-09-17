package com.shodhacode.repository;

import com.shodhacode.model.Submission;
import com.shodhacode.model.User;
import com.shodhacode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUser(User user);
    List<Submission> findByProblem(Problem problem);
}
