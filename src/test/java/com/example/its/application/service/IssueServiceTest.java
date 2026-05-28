package com.example.its.application.service;

import com.example.its.shared.dto.issue.CommentCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueTagUpdateRequest;
import com.example.its.util.TestDatabaseManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueServiceTest {

    private static IssueService issueService;

    @BeforeAll
    static void setUp() {
        issueService = new IssueService();
    }

    
    @BeforeEach
    void resetDatabaseBeforeEachTest() {
        TestDatabaseManager.resetDatabase();
    }

    @Test
    void testAddComment_Success() {
        // Given: 1번 이슈에 2번 계정(dev1)이 정상적인 코멘트를 작성하는 상황
        CommentCreateRequest request = new CommentCreateRequest(1L, 2L, "이것은 테스트 코멘트입니다.");

        // When: 실제 서비스 로직 실행
        IssueDetailResponse response = issueService.addComment(request);

        // Then: 에러 없이 정상 반환되었는지 검증
        assertNotNull(response);
        assertEquals(1L, response.getIssueId());
        
        // 추가) 아래처럼 코멘트가 잘 들어갔는지 내용까지 깐깐하게 검증 가능하나 일단은 보류
        // assertTrue(response.getComments().stream().anyMatch(c -> c.getContent().equals("이것은 테스트 코멘트입니다.")));
    }

    @Test
    void testAddComment_Fail_EmptyContent() {
        // Given: 내용이 텅 빈 코멘트 작성 시도 (엣지 케이스)
        CommentCreateRequest request = new CommentCreateRequest(1L, 2L, "   ");

        // When & Then: 코멘트 내용이 비어있으면 IllegalArgumentException이 발동해야 정상
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> issueService.addComment(request)
        );
        assertTrue(exception.getMessage().contains("비어있습니다"));
    }

    @Test
    void testUpdateIssueTags_Success() {
        // Given: seed-data.sql에 따르면 현재 1번 이슈는 1번 태그('backend')를 달고 있음.
        // 1번 태그(backend)를 떼버리고, 2번 태그('ui')를 새로 붙이는 요청 생성
        IssueTagUpdateRequest request = new IssueTagUpdateRequest(
            1L,           // 이슈 ID
            List.of(2L),       // 새로 추가할 태그 ID 리스트
            List.of(1L)        // 기존에서 뺄 태그 ID 리스트
        );

        // When: 서비스 로직 실행
        IssueDetailResponse response = issueService.updateIssueTags(request);

        // Then: 오류 없이 잘 수행되었는지 확인
        assertNotNull(response);
        assertEquals(1L, response.getIssueId());
        assertEquals("ui", response.getTagNames().get(0));
    }
}