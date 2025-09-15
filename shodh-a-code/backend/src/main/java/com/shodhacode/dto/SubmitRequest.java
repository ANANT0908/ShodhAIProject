package com.shodhacode.dto;

public class SubmitRequest {
    private String contestId;
    private Long problemId;
    private String username;
    private String language;
    private String sourceCode;

    // getters/setters
    public String getContestId(){return contestId;}
    public void setContestId(String contestId){this.contestId = contestId;}
    public Long getProblemId(){return problemId;}
    public void setProblemId(Long problemId){this.problemId = problemId;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}
    public String getLanguage(){return language;}
    public void setLanguage(String language){this.language = language;}
    public String getSourceCode(){return sourceCode;}
    public void setSourceCode(String sourceCode){this.sourceCode = sourceCode;}
}
