package com.project.shift.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.entity.ChatroomUserEntity;
import com.project.shift.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@SpringBootTest
@Transactional
public class ChatroomUserLazyLoadingTest {

	@Autowired // JPA 작업을 위해 EntityManager 주입
    private EntityManager entityManager;
	
	@Autowired
    private ChatroomUserRepository chatroomUserRepository;
	
	private Statistics statistics; // SQL 실행 횟수 추적용 통계 객체
	
	@BeforeEach // 매 테스트 시작 전에 통계 객체를 초기화
    void setUp() {
        Session session = entityManager.unwrap(Session.class); // EntityManager -> Hibernate Session 변환
        statistics = session.getSessionFactory().getStatistics(); // SessionFactory 통계 객체 획득
        statistics.setStatisticsEnabled(true); // 통계 수집 활성화
        statistics.clear(); // 이전 테스트 통계 초기화
    }
	
	@Test // 연관관계가 없는 현재 구조 검증
    @DisplayName("연관관계가 없는 ChatroomUserEntity는 userId만 조회되고 UserEntity는 자동 로딩되지 않는다")
    void noRelation_userEntityIsNotAutoLoaded() {
        UserEntity user = saveUser("norelation_user", "연관관계없음", "010-1000-1000"); // 사용자 1건 저장
        saveChatroomUser(user.getUserId(), 1001L); // 해당 사용자의 채팅방 사용자 행 저장

        entityManager.flush(); // INSERT를 DB에 반영
        entityManager.clear(); // 1차 캐시 비움(순수 조회 동작 확인 목적)
        statistics.clear(); // 검증 전 SQL 카운터 초기화

        ChatroomUserEntity loaded = chatroomUserRepository.getChatroomUser(1001L, user.getUserId()) // 채팅방 사용자 조회
                .orElseThrow(); // 조회 실패 시 테스트 실패

        long selectAfterFind = statistics.getPrepareStatementCount(); // 조회 직후 SQL 개수 저장
        assertThat(selectAfterFind).isEqualTo(1L); // 채팅방 사용자 조회 1회만 발생해야 함

        long userId = loaded.getUserId(); // 단순 스칼라 컬럼(USER_ID) 접근
        assertThat(userId).isEqualTo(user.getUserId()); // 조회값 일치 검증
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(selectAfterFind); // 추가 SQL 없어야 함
    }

    @Test // LAZY 연관관계가 있을 때 지연 로딩 동작 검증
    @DisplayName("LAZY 연관관계를 추가한 매핑에서는 UserEntity 프록시가 생성되고 접근 시점에 로딩된다")
    void withLazyRelation_userEntityLoadsOnAccess() {
        UserEntity user = saveUser("lazyrelation_user", "연관관계있음", "010-2000-2000"); // 사용자 1건 저장
        saveChatroomUser(user.getUserId(), 2002L); // 채팅방 사용자 행 저장

        entityManager.flush(); // INSERT 반영
        entityManager.clear(); // 1차 캐시 비움
        statistics.clear(); // 검증용 SQL 카운터 초기화

        ChatroomUserWithLazyUserRelationEntity loaded = entityManager.createQuery( // 테스트 전용 엔티티 매핑으로 조회
                        "select c from ChatroomUserWithLazyUserRelationEntity c where c.userId = :userId", // USER_ID 조건 조회
                        ChatroomUserWithLazyUserRelationEntity.class)
                .setParameter("userId", user.getUserId()) // 파라미터 바인딩
                .getSingleResult(); // 단건 조회

        long selectAfterFind = statistics.getPrepareStatementCount(); // 1차 조회 SQL 개수
        assertThat(selectAfterFind).isEqualTo(1L); // 아직 UserEntity 로딩 전이므로 1회
        assertThat(Hibernate.isInitialized(loaded.getUser())).isFalse(); // 연관 User는 미초기화(프록시)

        String userName = loaded.getUser().getName(); // 프록시 내부 필드 접근(지연 로딩 트리거)
        assertThat(userName).isEqualTo("연관관계있음"); // 로딩된 사용자 이름 검증
        assertThat(Hibernate.isInitialized(loaded.getUser())).isTrue(); // 접근 후 초기화 완료 확인
        assertThat(statistics.getPrepareStatementCount()).isGreaterThan(selectAfterFind); // 추가 SQL 발생 확인
    }
    
    @Test // 연관 경로 조건(c.user.userId)으로 조회했을 때 초기화 여부 검증
    @DisplayName("where c.user.userId = :userId 조회 시점에는 user가 로딩되지 않고 접근 시 로딩된다")
    void withOnlyRelationField_queryByUserPath_userLoadsOnAccess() {
        UserEntity user = saveUser("relationpath_user", "연관경로조회", "010-4000-4000"); // 사용자 저장
        Long chatroomId = 4004L; // 테스트 채팅방 ID
        saveChatroomUser(user.getUserId(), chatroomId); // 채팅방 사용자 저장

        entityManager.flush(); // INSERT 반영
        entityManager.clear(); // 1차 캐시 비움
        statistics.clear(); // SQL 카운터 초기화

        ChatroomUserWithOnlyUserRelationEntity loaded = entityManager.createQuery( // user 연관 경로로 조건 조회
                        "select c from ChatroomUserWithOnlyUserRelationEntity c where c.user.userId = :userId",
                        ChatroomUserWithOnlyUserRelationEntity.class)
                .setParameter("userId", user.getUserId()) // 사용자 ID 바인딩
                .getSingleResult(); // 단건 조회

        long selectAfterFind = statistics.getPrepareStatementCount(); // 조회 직후 SQL 개수
        assertThat(selectAfterFind).isEqualTo(1L); // 조건 조회 SQL 1회
        assertThat(Hibernate.isInitialized(loaded.getUser())).isFalse(); // user는 여전히 미초기화 프록시

        String userName = loaded.getUser().getName(); // 실제 user 필드 접근(지연 로딩 트리거)
        assertThat(userName).isEqualTo("연관경로조회"); // 사용자명 검증
        assertThat(Hibernate.isInitialized(loaded.getUser())).isTrue(); // 접근 후 초기화 확인
        assertThat(statistics.getPrepareStatementCount()).isGreaterThan(selectAfterFind); // 추가 SQL 발생 확인
    }

    private UserEntity saveUser(String loginId, String name, String phone) { // 공통 사용자 저장 헬퍼
        UserEntity user = UserEntity.builder() // 빌더로 사용자 엔티티 생성
                .loginId(loginId) // 로그인 아이디 설정
                .password("encoded-password") // 테스트용 비밀번호(인코딩 문자열 가정)
                .name(name) // 이름 설정
                .phone(phone) // 전화번호 설정
                .address("seoul") // 주소 설정
                .points(0) // 초기 포인트
                .adminFlag("N") // 일반 사용자 플래그
                .build(); // 엔티티 생성 완료

        entityManager.persist(user); // 영속화
        return user; // 저장된 엔티티 반환
    }

    private void saveChatroomUser(Long userId, Long chatroomId) { // 공통 채팅방 사용자 저장 헬퍼
    	saveChatroom(chatroomId);
    	
        ChatroomUserEntity chatroomUser = ChatroomUserEntity.builder() // 빌더로 채팅방 사용자 생성
                .chatroomId(chatroomId) // 채팅방 ID 설정
                .userId(userId) // 사용자 ID(FK 값) 설정
                .chatroomName("테스트 채팅방") // 채팅방 이름 설정
                .createdTime(new Date()) // 생성 시각 설정
                .lastConnectionTime(new Date()) // 마지막 접속 시각 설정
                .connectionStatus("ON") // 접속 상태 ON
                .isDarkMode("N") // 다크모드 OFF
                .build(); // 엔티티 생성 완료

        entityManager.persist(chatroomUser); // 영속화
    }
    
    private void saveChatroom(Long chatroomId) { // CHATROOM_USERS의 부모 테이블 데이터 생성 헬퍼
        entityManager.createNativeQuery( // 운영 DDL 기준으로 CHATROOMS 행 직접 삽입
                        "INSERT INTO CHATROOMS (CHATROOM_ID, LAST_MSG_CONTENT, LAST_MSG_DATE) VALUES (:chatroomId, :content, :lastMsgDate)")
                .setParameter("chatroomId", chatroomId) // 부모 키 지정
                .setParameter("content", "테스트 마지막 메시지") // 마지막 메시지 내용
                .setParameter("lastMsgDate", new Date()) // 마지막 메시지 시간
                .executeUpdate(); // INSERT 실행
    }

    @Entity(name = "ChatroomUserWithLazyUserRelationEntity") // 테스트 전용 엔티티 이름
    @Table(name = "CHATROOM_USERS") // 실제 CHATROOM_USERS 테이블 재사용
    static class ChatroomUserWithLazyUserRelationEntity {

        @Id // PK 지정
        @Column(name = "CHATROOM_USERS_ID") // PK 컬럼 매핑
        private Long chatroomUserId;

        @Column(name = "USER_ID") // USER_ID 컬럼 매핑
        private Long userId;

        @ManyToOne(fetch = FetchType.LAZY) // UserEntity를 지연 로딩으로 매핑
        @JoinColumn(name = "USER_ID", referencedColumnName = "user_id", insertable = false, updatable = false) // 읽기 전용 조인
        private UserEntity user;

        public UserEntity getUser() { // 테스트에서 User 프록시 접근용 getter
            return user;
        }
    }
    
    @Entity(name = "ChatroomUserWithOnlyUserRelationEntity") // USER_ID 스칼라 없이 연관관계만 갖는 테스트 전용 엔티티
    @Table(name = "CHATROOM_USERS") // 실제 CHATROOM_USERS 테이블 재사용
    static class ChatroomUserWithOnlyUserRelationEntity {

        @Id // PK 지정
        @Column(name = "CHATROOM_USERS_ID") // PK 컬럼 매핑
        private Long chatroomUserId;

        @ManyToOne(fetch = FetchType.LAZY) // USER_ID를 UserEntity로만 매핑
        @JoinColumn(name = "USER_ID", referencedColumnName = "user_id") // 읽기/쓰기 가능한 연관관계
        private UserEntity user;

        public UserEntity getUser() { // 연관 사용자 getter
            return user;
        }
    }
}
