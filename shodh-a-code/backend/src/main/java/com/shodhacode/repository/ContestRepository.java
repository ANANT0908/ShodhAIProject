package com.shodhacode.repository;

import com.shodhacode.model.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, Long> {
   
}
