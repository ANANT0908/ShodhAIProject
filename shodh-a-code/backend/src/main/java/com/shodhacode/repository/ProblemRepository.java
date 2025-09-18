package com.shodhacode.repository;

import com.shodhacode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    /**
     * Load a Problem together with its testCases in one query to avoid LazyInitializationException
     * when used outside a transaction/session (eg: in worker threads).
     */
    @Query("select p from Problem p left join fetch p.testCases where p.id = :id")
    Optional<Problem> findByIdWithTestCases(@Param("id") Long id);
}
