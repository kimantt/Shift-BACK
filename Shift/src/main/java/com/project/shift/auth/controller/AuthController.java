package com.project.shift.auth.controller;

import com.project.shift.auth.dto.request.LoginRequestDTO;
import com.project.shift.auth.dto.response.AccessTokenResponseDTO;
import com.project.shift.auth.dto.response.LoginResponseDTO;
import com.project.shift.auth.dto.response.LogoutResponseDTO;
import com.project.shift.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final String HEADER = "Authorization";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 로그인 기능
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> userLogin(@Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {
        log.info("[AUTH] 로그인 시도 User ID: {}", request.loginId());
        
        LoginResponseDTO tokens = authService.login(request);
        log.info("[AUTH] 로그인 성공 User ID: {}", request.loginId());

        ResponseCookie cookie = authService.createRefreshTokenCookie(tokens.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AccessTokenResponseDTO(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout() {
        authService.logout();

        // 로그아웃 시 쿠키 삭제
        ResponseCookie deleteCookie = authService.createDeleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .header("Clear-Site-Data", "\"cookies\", \"storage\", \"cache\"") // 좀비 쿠키 방지를 위한 추가 헤더
                .body(new LogoutResponseDTO("로그아웃이 정상적으로 처리되었습니다."));
    }

    // Access 토큰 재발급 기능
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDTO> refreshToken(@RequestHeader(value = HEADER, required = false) String authorizationHeader,
                                          					   @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // 토큰 재발급 서비스 호출
    	LoginResponseDTO tokens = authService.refresh(authorizationHeader, refreshToken);

        // 새로운 리프레시 토큰 쿠키 생성
        ResponseCookie newRefreshCookie = authService.createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(new AccessTokenResponseDTO(tokens.accessToken()));
    }
}
