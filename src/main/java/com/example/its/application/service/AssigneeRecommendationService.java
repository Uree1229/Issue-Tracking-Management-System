package com.example.its.application.service;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.entity.Tag;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.IssueRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.issue.RecommendationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Feat: 개발자 추천 알고리즘 구현을 위한 라이브러리들
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AssigneeRecommendationService {

    public AssigneeRecommendationService() {
    }

    // 1. 태그 기반 담당자(DEV) 추천 알고리즘 (초기 구현, 나중에 더 고도화할 예정)
    public List<RecommendationResponse> recommendAssignees(Long projectId, List<Long> targetTagIds) {
        return TransactionManager.execute(entityManager -> {
            AccountRepository accountRepository = new AccountRepository(entityManager);
            IssueRepository issueRepository = new IssueRepository(entityManager);

            // Step 1. 후보군 추출: 시스템 내 모든 활성 상태의 DEV 계정
            List<Account> devAccounts = accountRepository.findByRole(Role.DEV).stream()
                    .filter(Account::isActive)
                    .toList();

            List<RecommendationResponse> recommendations = new ArrayList<>();

            // Step 2. 각 DEV 후보별 스코어링 계산
            for (Account dev : devAccounts) {
                double score = calculateScore(issueRepository, dev, projectId, targetTagIds);
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
        });
    }

    // 2. 가중치 계산 및 점수 산출 모델 (다중 팩터 통합 알고리즘)
    /**
    본 소프트웨어의 ‘Tag 기반 담당 개발자 추천 엔진’은 단순히 태그의 일치 여부만을 확인하는 것이 아닌,
    데이터의 시계열적 가치와 현재 업무의 부하를 종합적으로 평가하는 
    다중 팩터 모델(Multi-Factor Model)을 채택하고 있습니다. 

    아래는 개발자 추천을 위한 구체적인 3요소입니다.

    1. Risk-Weighted Workload (리스크 차등 패널티)
    현재 진행 중인 이슈의 Priority를 기준으로 페널티 점수를 부여합니다. 
    즉, 현재 Priority가 높은 이슈를 검토/해결중인 개발자는 추천 대상에서 멀어지게 됩니다. 
    구체적인 패널티 점수 수치는 아래와 같습니다. 
    - BLOCKER/CRITICAL (-8)
    - MAJOR (-5)
    - MINOR (-1)

    2. Time-Decay Factor (시계열 반감기)
    과거에 해결한 이슈일수록 디테일을 까먹을 가능성이 높겠죠? 
    같은 경험치이면 되도록 최근에 동종 이슈를를 해결한 개발자가 더 빠르게 이슈를 해결할 수 있을 것입니다.
    따라서, 동일한 이슈 해결 경험이더라도 시간이 오래될수록(더 과거일수록) 점수를 깎습니다.
    - 주(Week) 단위로 10%씩 경험치 가중치를 지수 감소(Exponential Decay)시킴. 
    - (단, 0으로 수렴하지 않도록 최솟값은 10%로 설정
    
    3. Coverage Ratio (전문성 커버리지)
    - Jaccard Similarity 개념을 차용하여, 
    - 타겟 이슈의 태그 중 후보 개발자가 다루어본 태그의 비율(%)을 계산하여 점수에 반영합니다. 
    - 즉, 가장 근본적으로 해당 이슈와 유사한 이슈를 해결해본 개발자를 추천하도록 합니다.

    위 3요소의 조화를 통해, 사용자는 현재 업무 병목이 없으며, 
    과거에 유사한 이슈 해결 경험이 있고, 해당 이슈 해결 경험이 되도록 최근에 있었던 
    최적의 개발자를 추천받을 수 있습니다.
    
    * @param issueRepository DB 조회를 위한 Repository
    * @param dev 평가 대상 개발자의 계정
    * @param projectId 현재 할당하려는 이슈가 속한 프로젝트 ID
    * @param targetTagIds 현재 할당하려는 이슈의 요구 기술 스택(Tag) 목록
    * @return 0 이상의 최종 추천 점수 (높을수록 적합하다는 의미)
    **/

    private double calculateScore(IssueRepository issueRepository, Account dev, Long projectId, List<Long> targetTagIds) {
        double score = 50.0; // 기본 점수

        // DEV가 과거에 담당했던 혹은 현재 담당 중인 모든 이슈 조회
        List<Issue> assignedIssues = issueRepository.findByAssigneeAccountId(dev.getAccountId());

        double workloadPenalty = 0.0;
        double experienceScore = 0.0;

        for (Issue issue : assignedIssues) {
            
            // [Factor 1] 리스크 기반 업무 부하량 패널티 (Priority-Weighted Workload)
            if (issue.getStatus() == IssueStatus.ASSIGNED || issue.getStatus() == IssueStatus.REOPENED) {
                double penaltyWeight = 3.0; // 기본 패널티
                
                // Priority Enum 이름 기반 안전한 가중치 매핑
                if (issue.getPriority() != null) {
                    String pName = issue.getPriority().name();
                    if (pName.contains("BLOCKER") || pName.contains("CRITICAL") || pName.contains("HIGHEST")) {
                        penaltyWeight = 8.0;
                    } else if (pName.contains("MAJOR") || pName.contains("HIGH")) {
                        penaltyWeight = 5.0;
                    } else if (pName.contains("MINOR") || pName.contains("LOW")) {
                        penaltyWeight = 1.0;
                    }
                }
                workloadPenalty += penaltyWeight;
            }

            // [Factor 2 & 3] 시계열 반감기가 적용된 전문성 커버리지 (Time-Decay Experience)
            if (issue.getStatus() == IssueStatus.FIXED || issue.getStatus() == IssueStatus.RESOLVED || issue.getStatus() == IssueStatus.CLOSED) {
                
                // 1) 시간 감가상각 비율 계산 (Time-Decay)
                long weeksPassed = ChronoUnit.WEEKS.between(issue.getLastModifiedAt(), LocalDateTime.now());
                // 매주 10%씩 가중치 감소 (단, 아무리 오래되어도 최소 10%의 가중치는 보존하도록 설정)
                double decayFactor = Math.max(Math.pow(0.9, weeksPassed), 0.1); 

                // 2) 프로젝트 경험 점수 (+3점 * 반감기)
                if (issue.getProject().getProjectId().equals(projectId)) {
                    experienceScore += (3.0 * decayFactor);
                }

                // 3) 태그 커버리지 점수 (+10점 * 커버리지 비율 * 반감기)
                if (targetTagIds != null && !targetTagIds.isEmpty()) {
                    Set<Long> issueTagIds = issue.getTags().stream().map(Tag::getTagId).collect(Collectors.toSet());
                    
                    long matchCount = 0;
                    for (Long targetId : targetTagIds) {
                        if (issueTagIds.contains(targetId)) {
                            matchCount++;
                        }
                    }
                    
                    // Jaccard 유사도 개념: 타겟 태그 중 몇 %를 커버하는가?
                    double coverageRatio = (double) matchCount / targetTagIds.size();
                    experienceScore += (10.0 * coverageRatio * decayFactor);
                }
            }
        }

        // 최종 점수 산출
        score += experienceScore;
        score -= workloadPenalty;

        // 최하 0점 보장 (마이너스 점수 방지)
        return Math.max(score, 0.0);
    }
}


