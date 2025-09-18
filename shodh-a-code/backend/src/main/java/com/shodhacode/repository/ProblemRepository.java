package com.shodhacode.repository;

import com.shodhacode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("select p from Problem p left join fetch p.testCases where p.id = :id")
    Optional<Problem> findByIdWithTestCases(@Param("id") Long id);
}
