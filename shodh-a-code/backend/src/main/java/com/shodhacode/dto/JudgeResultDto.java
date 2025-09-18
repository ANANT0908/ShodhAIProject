package com.shodhacode.dto;

public class JudgeResultDto {
    private String status;
    private String details;
    private Integer passedCount;
    private Integer totalCount;

    public JudgeResultDto(){}
    public JudgeResultDto(String status, String details, Integer passedCount, Integer totalCount){
        this.status = status; this.details = details; this.passedCount = passedCount; this.totalCount = totalCount;
    }


    public String getStatus(){return status;}
    public void setStatus(String status){this.status = status;}
    public String getDetails(){return details;}
    public void setDetails(String details){this.details = details;}
    public Integer getPassedCount(){return passedCount;}
    public void setPassedCount(Integer passedCount){this.passedCount = passedCount;}
    public Integer getTotalCount(){return totalCount;}
    public void setTotalCount(Integer totalCount){this.totalCount = totalCount;}
}
