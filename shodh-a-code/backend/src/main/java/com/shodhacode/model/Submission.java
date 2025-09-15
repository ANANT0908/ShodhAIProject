package com.shodhacode.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="submission")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="submission_uuid", unique=true)
    private String submissionUuid;

    private String username;
    private String language;

    @Column(name="source_code", columnDefinition="text")
    private String sourceCode;

    private String status; // Pending, Running, Accepted, Wrong Answer, TLE, Error

    @Column(name="result_details", columnDefinition="text")
    private String resultDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="problem_id")
    private Problem problem;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    private Integer score;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getSubmissionUuid(){return submissionUuid;}
    public void setSubmissionUuid(String submissionUuid){this.submissionUuid = submissionUuid;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}
    public String getLanguage(){return language;}
    public void setLanguage(String language){this.language = language;}
    public String getSourceCode(){return sourceCode;}
    public void setSourceCode(String sourceCode){this.sourceCode = sourceCode;}
    public String getStatus(){return status;}
    public void setStatus(String status){this.status = status;}
    public String getResultDetails(){return resultDetails;}
    public void setResultDetails(String resultDetails){this.resultDetails = resultDetails;}
    public Problem getProblem(){return problem;}
    public void setProblem(Problem problem){this.problem = problem;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant createdAt){this.createdAt = createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(Instant updatedAt){this.updatedAt = updatedAt;}
    public Integer getScore(){return score;}
    public void setScore(Integer score){this.score = score;}
}
