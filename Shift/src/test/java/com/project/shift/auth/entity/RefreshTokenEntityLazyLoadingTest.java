package com.project.shift.auth.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.auth.repository.RefreshTokenRepository;
import com.project.shift.user.entity.UserEntity;
import com.project.shift.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

@SpringBootTest
@Transactional
class RefreshTokenEntityLazyLoadingTest {

	@Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("RefreshTokenEntity 조회 시 UserEntity는 지연 로딩된다")
    void userEntityShouldBeLazyLoadedFromRefreshTokenEntity() {
        // given: 테스트 데이터 준비 단계
    	
    	// 중복 방지용 8자리 랜덤 접미사 생성
        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 사용자 엔티티 생성 후 저장
        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .loginId("lazy" + uniqueSuffix)
                .password("encoded-password")
                .name("Lazy User")
                .phone("010" + uniqueSuffix)
                .address("Seoul")
                .build());

        // 리프레시 토큰 엔티티 생성 후 저장
        RefreshTokenEntity savedRefreshToken = refreshTokenRepository.save(RefreshTokenEntity.builder()
                .user(savedUser)
                .refreshToken("refresh-token-value")
                .build());

        // INSERT SQL을 DB에 즉시 반영
        entityManager.flush();
        // 1차 캐시 비워 재조회 시 프록시/로딩 상태를 정확히 검증
        entityManager.clear();

        // when: 테스트 대상 동작 수행
        
        // PK(userId)로 리프레시 토큰 재조회
        RefreshTokenEntity foundRefreshToken = refreshTokenRepository.findById(savedRefreshToken.getUserId())
                .orElseThrow(); // 없으면 예외 발생
        // 로딩 여부 확인 유틸 획득
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        // then: 기대 결과 검증
        // 조회 직후 user 연관관계는 아직 미로딩이어야 함
        assertThat(persistenceUnitUtil.isLoaded(foundRefreshToken, "user")).isFalse();

        // user 접근 시점에 LAZY 로딩 트리거
        String loginId = foundRefreshToken.getUser().getLoginId();

        // 로딩된 user의 loginId가 저장값과 동일한지 확인
        assertThat(loginId).isEqualTo(savedUser.getLoginId());
        // user 접근 이후 로딩 완료 상태여야 함
        assertThat(persistenceUnitUtil.isLoaded(foundRefreshToken, "user")).isTrue();
    }
}
