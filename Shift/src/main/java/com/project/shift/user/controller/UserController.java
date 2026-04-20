package com.project.shift.user.controller;

import com.project.shift.global.exception.detail.UserValidationException;
import com.project.shift.shop.dto.PointHistoryResponseDTO;
import com.project.shift.shop.service.IOrderService;
import com.project.shift.user.dto.LoginIdRequestDTO;
import com.project.shift.user.dto.UserDTO;
import com.project.shift.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
    	//서버 회원가입 요청
    	Long userId = userService.join(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공. 할당된 사용자 ID:" + userId);
    }

    // 연락처 중복 확인
    @PostMapping("/check/phone")
    public ResponseEntity<?> checkPhone(@RequestBody Map<String, String> request) {
    	String phone = request.get("phone");
        boolean isDuplicate = userService.isPhoneAvailable(phone);
        return ResponseEntity.ok(Map.of(
                "available", !isDuplicate,
                "message", isDuplicate ? "이미 사용중인 연락처입니다." : "사용 가능한 연락처입니다."
        ));
    }

    // 아이디 중복 확인
    @PostMapping("/check")
    public ResponseEntity<?> checkLoginId(@RequestBody Map<String, String> request) {
    	String loginId = request.get("loginId");
        boolean isDuplicate = userService.isLoginIdAvailable(loginId);
        return ResponseEntity.ok(Map.of(
                "available", !isDuplicate,
                "message", isDuplicate ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다."
        ));
    }

    // 비밀번호 보안 규칙 검증
    @PostMapping("/check/pw-rule")
    public ResponseEntity<?> checkPasswordRule(@RequestBody Map<String, String> request) {
    	String password = request.get("password");
        userService.validatePasswordRule(password);
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "사용 가능한 비밀번호입니다."
        ));
    }

    // 본인 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo() {
        UserDTO user = userService.getUserInfo();
        return ResponseEntity.ok(user);
    }

    // 본인 정보 수정
    @PutMapping("/info")
    public ResponseEntity<UserDTO> updateMyInfo(
            @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUserInfo(userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody LoginIdRequestDTO loginIdRequestDTO) {
        String loginId = userService.findId(loginIdRequestDTO);
        return ResponseEntity.ok(Map.of("loginId", loginId));
    }

    // SHOP-011 포인트 사용/적립 내역 조회
    @GetMapping("/points-history")
    public ResponseEntity<PointHistoryResponseDTO> getPointHistory() {
    	UserDTO user = userService.getUserInfo();
        return ResponseEntity.ok(orderService.getPointHistory(user.userId()));
    }

    // 마이포인트 조회
    @GetMapping("/points")
    public ResponseEntity<?> getMyPoints() {
    	UserDTO user = userService.getUserInfo();
        return ResponseEntity.ok(Map.of(
                "points", user.points()
        ));
    }

    // 비밀번호 인증
    @PostMapping("/check/password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null || password.isBlank()) {
        	throw new UserValidationException("비밀번호를 입력해주세요.");
        }

        boolean isValid = userService.verifyPassword(password);
        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "비밀번호 인증에 성공했습니다." : "비밀번호가 일치하지 않습니다."
        ));
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<?> withdrawUser() {
    	userService.withdrawUser();
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
    }
}