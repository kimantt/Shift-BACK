package com.project.shift.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.shift.auth.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

}
