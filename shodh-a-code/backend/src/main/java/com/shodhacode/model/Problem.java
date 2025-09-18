package com.shodhacode.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "problem")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    @JsonBackReference // 🔑 Prevents infinite recursion with Contest
    private Contest contest;

    @OneToMany(
        mappedBy = "problem",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JsonManagedReference // 🔑 Pairs with @JsonBackReference in TestCaseEntity
    private List<TestCaseEntity> testCases;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Contest getContest() { return contest; }
    public void setContest(Contest contest) { this.contest = contest; }

    public List<TestCaseEntity> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseEntity> testCases) { this.testCases = testCases; }
}
