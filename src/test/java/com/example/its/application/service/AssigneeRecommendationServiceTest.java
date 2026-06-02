package com.example.its.application.service;

import com.example.its.shared.dto.issue.RecommendationResponse;
import com.example.its.util.TestDatabaseManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssigneeRecommendationServiceTest {

    private static AssigneeRecommendationService recommendationService;

    @BeforeAll
    static void setUp() {
        // TestDatabaseManager.resetDatabase();
        recommendationService = new AssigneeRecommendationService();
    }

    // 윈도우 환경에서는 아래 메서드가 작동하지 않을 수 있습니다.
    @BeforeEach
    void resetDatabaseBeforeEachTest() {
        TestDatabaseManager.resetDatabase();
    }

    @Test
    void testCalculateScore_WithWorkloadPenalty() {
        // Given: 1번 프로젝트에 대해 담당자 추천을 요청. 타겟 태그는 1번('backend')
        // [현재 seed-data.sql의 수학적 상태]
        // 1. 후보자: 시스템 내 활성 DEV는 2번 계정('dev1') 1명뿐임.
        // 2. 업무량: dev1은 현재 2번 이슈(우선순위: MINOR, 상태: ASSIGNED)를 진행 중.
        // 3. 점수 계산: 기본점수(50) - MINOR패널티(1) + 경험치(0) = 49.0점
        
        List<Long> targetTagIds = List.of(1L);
        Long projectId = 1L;

        // When: 개발자 추천 알고리즘 실행
        List<RecommendationResponse> responses = recommendationService.recommendAssignees(projectId, targetTagIds);

        // Then: 추천된 개발자 목록과 점수가 정확한지 검증
        assertNotNull(responses);
        assertEquals(1, responses.size(), "활성화된 DEV가 1명이므로 리스트 사이즈는 1이어야 합니다.");

        RecommendationResponse topDev = responses.get(0);
        assertEquals(2L, topDev.getAccountId());
        assertEquals("dev1", topDev.getLoginId());
        
        // 부동소수점 오차(0.01) 이내에서 49.0점과 정확히 일치하는지 확인
        assertEquals(49.0, topDev.getScore(), 0.01, "MINOR 업무를 1개 들고 있으므로 최종 점수는 49.0이어야 합니다.");
    }
}