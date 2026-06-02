package com.example.its.shared.dto.issue;

public class RecommendationResponse {

    private Long accountId;
    private String loginId;
    private String name;
    private double score;

    public RecommendationResponse() {
    }

    public RecommendationResponse(Long accountId, String loginId, String name, double score) {
        this.accountId = accountId;
        this.loginId = loginId;
        this.name = name;
        this.score = score;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
