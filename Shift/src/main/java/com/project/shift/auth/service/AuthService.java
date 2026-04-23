package com.project.shift.auth.service;

import com.project.shift.auth.dto.request.LoginRequestDTO;
import com.project.shift.auth.dto.response.LoginResponseDTO;
import com.project.shift.auth.entity.RefreshTokenEntity;
import com.project.shift.auth.repository.AuthRepository;
import com.project.shift.auth.repository.RefreshTokenRepository;
import com.project.shift.global.exception.detail.auth.AuthUnauthorizedException;
import com.project.shift.global.exception.detail.auth.AuthValidationException;
import com.project.shift.global.exception.detail.user.UserNotFoundException;
import com.project.shift.global.jwt.JwtService;
import com.project.shift.user.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
	
	private static final String TOKEN_HEADER = "Bearer ";

	private final AuthRepository authRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(AuthRepository authRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
		this.authRepository = authRepository;
		this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // 로그인
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginInfo) {
        // 입력값 검증 수행
        UsernamePasswordAuthenticationToken cred = new UsernamePasswordAuthenticationToken(
                loginInfo.loginId(),
                loginInfo.password()
        );

        Authentication authentication = authenticationManager.authenticate(cred);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // dto -> entity로 변환
        UserEntity foundUser = authRepository.findByLoginId(loginInfo.loginId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Long userId = foundUser.getUserId();
        String name = foundUser.getName();

        log.info("[AUTH] 인증 성공, 토큰 발급 및 리프레시 토큰 갱신 시작 UserId: {}", userId);

        String accessToken = jwtService.createAccessToken(userId, name);
        String refreshToken = jwtService.createRefreshToken(userId);

        saveRefreshToken(foundUser, refreshToken);

        log.info("[AUTH] 리프레시 토큰 갱신 완료 UserId: {}", userId);

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    @Transactional
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        
        log.info("[AUTH] 로그아웃 시작 UserId: {}", userId);
        
        // DB의 리프레시 토큰 삭제
        refreshTokenRepository.deleteById(userId);

        log.info("[AUTH] 로그아웃 완료 UserId: {}", userId);
    }

    // 토큰 재발급
    @Transactional
    public LoginResponseDTO refresh(String authorizationHeader, String refreshToken) {
    	// refresh token 검증
    	String accessToken = extractAccessToken(authorizationHeader);
        validateRefreshToken(refreshToken);

        // 토큰의 값(userId)이 서로 일치하는지 체크
        Long userId = validateTokenPair(accessToken, refreshToken);

        // DB의 정보와 같은지 체크
        UserEntity foundUser = validateUserByToken(userId, refreshToken);

        // 토큰 재발급 실행
        String newAccessToken = jwtService.createAccessToken(foundUser.getUserId(), foundUser.getName());
        String newRefreshToken = jwtService.createRefreshToken(foundUser.getUserId());

        // DB값 갱신
        saveRefreshToken(foundUser, newRefreshToken);

        return new LoginResponseDTO(newAccessToken, newRefreshToken);
    }
    
    // 리프레시 토큰 쿠키를 생성하는 메서드
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    // 리프레시 토큰 삭제용 쿠키를 생성하는 메서드
    public ResponseCookie createDeleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .path("/auth/refresh")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
    }

    // Authorization 헤더에서 Access Token을 추출하는 메서드
    private String extractAccessToken(String authorizationHeader) {
        if (isInvalidAuthorizationHeader(authorizationHeader)) {
            throw new AuthValidationException("[SYSTEM] Access Token이 올바르지 않습니다.");
        }
        return authorizationHeader.replace(TOKEN_HEADER, "");
    }

    // Authorization 헤더의 형식 유효성을 검사하는 메서드
    private boolean isInvalidAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_HEADER);
    }

    // 리프레시 토큰의 존재 여부와 유효성을 검증하는 메서드
    private void validateRefreshToken(String refreshToken) {
    	if (refreshToken == null) {
            throw new AuthValidationException("[SYSTEM] 리프레시 토큰이 존재하지 않습니다.");
        }
    	
        // 토큰 유효성 체크
        if (!jwtService.isValidToken(refreshToken)) {
            throw new AuthUnauthorizedException("[SYSTEM] 유효하지 않은 리프레시 토큰입니다.");
        }
        // 토큰 타입이 refresh 인지 체크
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthUnauthorizedException("[SYSTEM] 토큰 타입이 리프레시 토큰이 아닙니다.");
        }
    }

    // Access Token과 Refresh Token의 사용자 일치 여부를 검증하는 메서드
    private Long validateTokenPair(String accessToken, String refreshToken) {
        Long userIdFromAccess = jwtService.extractUserIdFromExpiredValidToken(accessToken);
        if (userIdFromAccess == null) {
            throw new AuthUnauthorizedException("[SYSTEM] 신뢰할 수 없는 엑세스 토큰입니다.");
        }

        Long userIdFromRefresh = jwtService.extractUserIdFromValidToken(refreshToken);

        // 두 토큰의 짝이 맞는지 체크
        if (!userIdFromAccess.equals(userIdFromRefresh)) {
            throw new AuthUnauthorizedException("[SYSTEM] 토큰이 서로 일치하지 않습니다.");
        }
        return userIdFromRefresh;
    }

    // 토큰의 사용자와 저장된 리프레시 토큰 일치 여부를 검증하는 메서드
    private UserEntity validateUserByToken(Long userId, String refreshToken) {
        // userId로 사용자 조회
    	UserEntity foundUser = authRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String savedRefreshToken = refreshTokenRepository.findById(userId)
                .map(RefreshTokenEntity::getRefreshToken)
                .orElse(null);

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new AuthUnauthorizedException("[SYSTEM] 리프레시 토큰이 저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        return foundUser;
    }
    
    // 리프레시 토큰을 저장하거나 갱신하는 메서드
    private void saveRefreshToken(UserEntity userEntity, String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(userEntity.getUserId())
                .orElse(RefreshTokenEntity.builder()
                        .user(userEntity)
                        .build());
        refreshTokenEntity.updateRefreshToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }
}
