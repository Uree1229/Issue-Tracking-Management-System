package com.example.its.application.service;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.entity.Tag;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.IssueRepository;
import com.example.its.shared.dto.issue.RecommendationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ------ 가중치 부여 공식 ------
// TODO: 이건 좀 더 고도화된 알고리즘으로 업그레이드 할 예정입니다. 일단은 돌아가게만 만들어 뒀습니다.
// 도메인/기술 전문성 가중치 (+5점): 생성하려는 이슈와 동일한 태그(예: DB, UI, Network 등)가 달린 이슈를 과거에 해결한 적이 있다면 가장 큰 가점을 부여
// 프로젝트 이해도 가중치 (+2점): 해당 프로젝트 내에서 이슈를 해결한 이력이 있다면, 프로젝트 히스토리를 잘 안다고 판단해 소폭 가점
// 병목 현상(Workload) 방지 패널티 (-3점): 현재 ASSIGNED나 REOPENED 상태의 이슈를 많이 들고 있는 사람에게는 패널티를 주어 업무 과부하를 방지

public class AssigneeRecommendationService {

    private final AccountRepository accountRepository;
    private final IssueRepository issueRepository;

    public AssigneeRecommendationService(AccountRepository accountRepository, IssueRepository issueRepository) {
        this.accountRepository = accountRepository;
        this.issueRepository = issueRepository;
    }

    // 1. 태그 기반 담당자(DEV) 추천 알고리즘 (초기 구현, 나중에 더 고도화할 예정)
    public List<RecommendationResponse> recommendAssignees(Long projectId, List<Long> targetTagIds) {
        // Step 1. 후보군 추출: 시스템 내 모든 활성 상태의 DEV 계정
        List<Account> devAccounts = accountRepository.findByRole(Role.DEV).stream()
                .filter(Account::isActive)
                .toList();

        List<RecommendationResponse> recommendations = new ArrayList<>();

        // Step 2. 각 DEV 후보별 스코어링 계산
        for (Account dev : devAccounts) {
            double score = calculateScore(dev, projectId, targetTagIds);
            recommendations.add(new RecommendationResponse(
                    dev.getAccountId(),
                    dev.getLoginId(),
                    dev.getName(),
                    score
            ));
        }

        // Step 3. 점수 기준 내림차순 정렬 (가장 점수가 높은 사람이 1위)
        recommendations.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));

        // Step 4. 상위 5명만 끊어서 리턴
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }

    // 2. 가중치 계산 및 점수 산출 모델
    private double calculateScore(Account dev, Long projectId, List<Long> targetTagIds) {
        double score = 50.0; // 기본 점수

        // DEV가 과거에 담당했던 혹은 현재 담당 중인 모든 이슈 조회
        List<Issue> assignedIssues = issueRepository.findByAssigneeAccountId(dev.getAccountId());

        int currentWorkload = 0;
        int projectExperience = 0;
        int tagMatchCount = 0;

        for (Issue issue : assignedIssues) {
            // [팩터 1] 현재 업무 부하량 (진행 중인 이슈)
            if (issue.getStatus() == IssueStatus.ASSIGNED || issue.getStatus() == IssueStatus.REOPENED) {
                currentWorkload++;
            }

            // [팩터 2 & 3] 과거 완료(FIXED 이상)한 이슈를 통한 전문성 검증
            if (issue.getStatus() == IssueStatus.FIXED || issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
                
                // 동일 프로젝트 경험 여부
                if (issue.getProject().getProjectId().equals(projectId)) {
                    projectExperience++;
                }

                // 관련 태그(기술 스택 등) 처리 경험
                if (targetTagIds != null && !targetTagIds.isEmpty()) {
                    Set<Long> issueTagIds = issue.getTags().stream().map(Tag::getTagId).collect(Collectors.toSet());
                    for (Long targetTagId : targetTagIds) {
                        if (issueTagIds.contains(targetTagId)) {
                            tagMatchCount++;
                        }
                    }
                }
            }
        }

        // 맨위 가중치 부여 공식에 따라서 점수를 산출
        score += (tagMatchCount * 5.0);
        score += (projectExperience * 2.0);
        score -= (currentWorkload * 3.0);

        // 최하 0점 보장 (마이너스 점수 방지)
        return Math.max(score, 0.0);
    }
}