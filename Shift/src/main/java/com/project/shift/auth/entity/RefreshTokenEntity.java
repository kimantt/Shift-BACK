package com.project.shift.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refreshtokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

	@Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "refresh_token", nullable = false, length = 255)
    private String refreshToken;
    
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    public RefreshTokenEntity(Long userId, String refreshToken, LocalDateTime expiredAt) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
