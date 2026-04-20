package com.project.shift.auth.dao;

import com.project.shift.auth.entity.RefreshTokenEntity;
import com.project.shift.auth.repository.AuthRepository;
import com.project.shift.auth.repository.RefreshTokenRepository;
import com.project.shift.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public class AuthDAO implements IAuthDAO{

    private final AuthRepository authRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthDAO(AuthRepository authRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authRepository = authRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public UserEntity getUser(UserEntity userEntity) {
        return authRepository.findByLoginId(userEntity.getLoginId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    public UserEntity getUserById(Long userId) {
        return authRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    public void saveRefreshToken(UserEntity userEntity, String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(userEntity.getUserId())
                .orElse(RefreshTokenEntity.builder()
                        .user(userEntity)
                        .build());
        refreshTokenEntity.updateRefreshToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    @Override
    public String getRefreshToken(Long userId) {
        return refreshTokenRepository.findById(userId)
                .map(RefreshTokenEntity::getRefreshToken)
                .orElse(null);
    }

    @Override
    public void updateRefreshToken(Long userId) {
    	refreshTokenRepository.deleteById(userId);
    }
}
