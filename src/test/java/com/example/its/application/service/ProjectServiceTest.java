package com.example.its.application.service;

import com.example.its.application.mapper.ProjectMapper;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;
import com.example.its.util.TestDatabaseManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    private static ProjectService projectService;

    @BeforeAll
    static void setUp() {
        
        // ProjectService는 IssueService와 달리 내부에 기본 생성자가 없으므로, Mapper를 직접 삽입
        projectService = new ProjectService(new ProjectMapper());
    }

    // 윈도우 환경에서는 아래 메서드가 작동하지 않을 수 있습니다.
    @BeforeEach
    void resetDatabaseBeforeEachTest() {
        TestDatabaseManager.resetDatabase();
    }

    @Test
    void testCreateProjectWithTags_Success() {
        // Given: 1번 계정(Admin)이 새로운 프로젝트를 생성하면서 태그 2개를 동시에 추가하려는 상황
        ProjectCreateRequest request = new ProjectCreateRequest(
            "AI Trading Engine",
            "Next-gen automated trading project",
            1L,
            List.of("fastapi", "python")
        );

        // When: 생성 로직 실행
        ProjectResponse response = projectService.createProject(request);

        // Then: 프로젝트가 잘 생성되었는지 검증
        assertNotNull(response);
        assertEquals("AI Trading Engine", response.getName());
    }

    @Test
    void testUpdateProjectTags_Fail_WhenTagInUse() {
        // Given: seed-data 기준, 1번 태그('backend')는 1번 이슈가 사용 중
        // 1번 프로젝트에서 1번 태그를 지워달라고 악의적인(잘못된) 요청 생성
        ProjectTagUpdateRequest request = new ProjectTagUpdateRequest(
            1L,           // 프로젝트 ID
            null,     // 추가할 태그 이름 리스트 (없음)
            List.of(1L)          // 제거할 태그 ID 리스트 (1번 태그)
        );

        // When & Then: IllegalStateException을 정확히 터뜨리는지 확인
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> projectService.updateProjectTags(request)
        );
        
        // 에러 메시지에 "사용 중"이라는 단어가 포함되어 있는지 체크
        assertTrue(exception.getMessage().contains("사용 중"));
    }

    @Test
    void testUpdateProjectTags_Success() {
        // Given: 아무도 사용하지 않는 새로운 태그('frontend')를 1번 프로젝트에 추가만 하는 안전한 요청
        ProjectTagUpdateRequest request = new ProjectTagUpdateRequest(
            1L,
            List.of("frontend"),
            null
        );

        // When: 태그 업데이트 로직 실행
        ProjectResponse response = projectService.updateProjectTags(request);

        // Then: 에러 없이 정상적으로 객체가 반환되는지 확인
        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
    }
}