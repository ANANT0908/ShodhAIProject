package com.shodhacode.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "testcase") 
public class TestCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "input_data", nullable = false)
    private String inputData;

    @Column(name = "expected_output", nullable = false)
    private String expectedOutput;

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonBackReference 
    private Problem problem;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }
}
