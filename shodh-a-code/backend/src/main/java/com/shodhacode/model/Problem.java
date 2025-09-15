package com.shodhacode.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="problem")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slug;
    private String title;

    @Column(columnDefinition="text")
    private String statement;

    @Column(name="input_example", columnDefinition="text")
    private String inputExample;

    @Column(name="output_example", columnDefinition="text")
    private String outputExample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="contest_id")
    private Contest contest;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="problem_id")
    private List<TestCaseEntity> testCases;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getSlug(){return slug;}
    public void setSlug(String slug){this.slug = slug;}
    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}
    public String getStatement(){return statement;}
    public void setStatement(String statement){this.statement = statement;}
    public String getInputExample(){return inputExample;}
    public void setInputExample(String inputExample){this.inputExample = inputExample;}
    public String getOutputExample(){return outputExample;}
    public void setOutputExample(String outputExample){this.outputExample = outputExample;}
    public Contest getContest(){return contest;}
    public void setContest(Contest contest){this.contest = contest;}
    public java.util.List<TestCaseEntity> getTestCases(){return testCases;}
    public void setTestCases(java.util.List<TestCaseEntity> testCases){this.testCases = testCases;}
}

