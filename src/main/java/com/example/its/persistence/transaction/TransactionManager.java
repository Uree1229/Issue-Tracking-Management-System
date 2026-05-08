package com.example.its.persistence.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 서비스 계층에서 하나의 작업 단위를 하나의 트랜잭션으로 묶기 위한 유틸리티입니다.
 * 
 * Spring의 @Transactional과 같은 역할을 하는 클래스입니다. 
 *
 * 서비스 메서드에서는 repository를 필드로 들고 있지 말고, 이 클래스가 전달해 주는
 * EntityManager로 repository를 생성해서 사용해야 합니다. 그래야 조회, 도메인 변경,
 * 저장, DTO 변환이 모두 같은 영속성 컨텍스트 안에서 실행됩니다.
 *
 * 예시: 
 * return TransactionManager.execute(entityManager -> {
 *     AccountRepository accountRepository = new AccountRepository(entityManager);
 *     Account account = accountRepository.findById(accountId)
 *             .orElseThrow(...);
 *
 *     account.setName(newName);
 *     return accountMapper.toResponse(accountRepository.save(account));
 * });
 * 
 *
 * 트랜잭션 시작, 커밋, 롤백, EntityManager 종료는 모두 이 클래스가 담당합니다.
 * Repository는 전달받은 EntityManager로 DB 작업만 수행합니다.
 */
public final class TransactionManager {

    private TransactionManager() {
    }

    /**
     * 반환값이 있는 서비스 작업을 트랜잭션 안에서 실행합니다.
     *
     * action 내부에서는 반드시 파라미터로 받은 EntityManager를 repository 생성자에
     * 넘겨야 합니다. 기존에 만들어 둔 repository나 다른 EntityManager를 사용하면
     * 같은 트랜잭션에 참여하지 못합니다.
     *
     * action: 같은 EntityManager와 트랜잭션 안에서 실행할 작업
     * 리턴값: action이 반환한 결과
     */
    public static <R> R execute(Function<EntityManager, R> action) {
        try (EntityManager entityManager = PersistenceContextProvider.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                R result = action.apply(entityManager);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    /**
     * 반환값이 없는 서비스 작업을 트랜잭션 안에서 실행합니다.
     *
     * 삭제, 비활성화, 상태 변경처럼 service 메서드의 반환 타입이 void인 경우 사용합니다.
     */
    public static void executeVoid(Consumer<EntityManager> action) {
        execute(entityManager -> {
            action.accept(entityManager);
            return null;
        });
    }
}
