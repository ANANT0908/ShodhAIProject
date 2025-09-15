package com.shodhacode.model;

import jakarta.persistence.*;

@Entity
@Table(name="testcase")
public class TestCaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="input_data", columnDefinition="text")
    private String inputData;

    @Column(name="expected_output", columnDefinition="text")
    private String expectedOutput;

    private boolean hidden;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    public String getInputData(){return inputData;}
    public void setInputData(String inputData){this.inputData = inputData;}
    public String getExpectedOutput(){return expectedOutput;}
    public void setExpectedOutput(String expectedOutput){this.expectedOutput = expectedOutput;}
    public boolean isHidden(){return hidden;}
    public void setHidden(boolean hidden){this.hidden = hidden;}
}
