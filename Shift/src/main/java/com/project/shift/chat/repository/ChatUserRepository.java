package com.project.shift.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.shift.user.entity.UserEntity;

public interface ChatUserRepository extends JpaRepository<UserEntity, Long>{

	// userId로 친구 검색
	@Query("""
			SELECT u FROM UserEntity u
            WHERE u.userId IN :friendIds
            ORDER BY u.name ASC
			""")
     List<UserEntity> findUserInfoByIds(@Param("friendIds") List<Integer> friendIds);

	// 하이픈 없이 정확하게 일치하는 핸드폰 번호로만 친구 검색
	@Query("""
		    SELECT u FROM UserEntity u
		    WHERE u.phone = :phone
			""") 
	UserEntity findByPhoneFlexible(@Param("phone") String phone);

}
