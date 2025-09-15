package com.shodhacode.repository;

import com.shodhacode.model.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {
}
