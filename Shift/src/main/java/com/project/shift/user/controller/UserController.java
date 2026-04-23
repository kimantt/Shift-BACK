package com.project.shift.user.controller;

import com.project.shift.shop.dto.PointHistoryResponseDTO;
import com.project.shift.shop.service.IOrderService;
import com.project.shift.user.dto.request.LoginIdCheckRequestDTO;
import com.project.shift.user.dto.request.LoginIdRequestDTO;
import com.project.shift.user.dto.request.PasswordRuleCheckRequestDTO;
import com.project.shift.user.dto.request.PasswordVerifyRequestDTO;
import com.project.shift.user.dto.request.PhoneCheckRequestDTO;
import com.project.shift.user.dto.request.RegisterUserRequestDTO;
import com.project.shift.user.dto.request.UpdateUserInfoRequestDTO;
import com.project.shift.user.dto.response.AvailabilityCheckResponseDTO;
import com.project.shift.user.dto.response.LoginIdFindResponseDTO;
import com.project.shift.user.dto.response.MessageResponseDTO;
import com.project.shift.user.dto.response.PasswordCheckResponseDTO;
import com.project.shift.user.dto.response.PointsResponseDTO;
import com.project.shift.user.dto.response.UserInfoResponseDTO;
import com.project.shift.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> registerUser(@Valid @RequestBody RegisterUserRequestDTO userDTO) {
    	//서버 회원가입 요청
    	Long userId = userService.join(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
        		.body(new MessageResponseDTO("회원가입 성공. 할당된 사용자 ID:" + userId));
    }

    // 연락처 중복 확인
    @PostMapping("/check/phone")
    public ResponseEntity<AvailabilityCheckResponseDTO> checkPhone(@Valid @RequestBody PhoneCheckRequestDTO request) {
    	String phone = request.phone();
        boolean isDuplicate = userService.isPhoneAvailable(phone);
        return ResponseEntity.ok(new AvailabilityCheckResponseDTO(
                !isDuplicate,
                isDuplicate ? "이미 사용중인 연락처입니다." : "사용 가능한 연락처입니다."
        ));
    }

    // 아이디 중복 확인
    @PostMapping("/check")
    public ResponseEntity<AvailabilityCheckResponseDTO> checkLoginId(@Valid @RequestBody LoginIdCheckRequestDTO request) {
    	String loginId = request.loginId();
        boolean isDuplicate = userService.isLoginIdAvailable(loginId);
        return ResponseEntity.ok(new AvailabilityCheckResponseDTO(
                !isDuplicate,
                isDuplicate ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다."
        ));
    }

    // 비밀번호 보안 규칙 검증
    @PostMapping("/check/pw-rule")
    public ResponseEntity<PasswordCheckResponseDTO> checkPasswordRule(@Valid @RequestBody PasswordRuleCheckRequestDTO request) {
    	String password = request.password();
        userService.validatePasswordRule(password);
        return ResponseEntity.ok(new PasswordCheckResponseDTO(true, "사용 가능한 비밀번호입니다."));
    }

    // 본인 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDTO> getMyInfo() {
    	UserInfoResponseDTO user = userService.getUserInfo();
        return ResponseEntity.ok(user);
    }

    // 본인 정보 수정
    @PutMapping("/info")
    public ResponseEntity<UserInfoResponseDTO> updateMyInfo(
    		@Valid @RequestBody UpdateUserInfoRequestDTO userDTO) {
    	UserInfoResponseDTO updatedUser = userService.updateUserInfo(userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<LoginIdFindResponseDTO> findId(@Valid @RequestBody LoginIdRequestDTO loginIdRequestDTO) {
        String loginId = userService.findId(loginIdRequestDTO);
        return ResponseEntity.ok(new LoginIdFindResponseDTO(loginId));
    }

    // SHOP-011 포인트 사용/적립 내역 조회
    @GetMapping("/points-history")
    public ResponseEntity<PointHistoryResponseDTO> getPointHistory() {
    	UserInfoResponseDTO user = userService.getUserInfo();
        return ResponseEntity.ok(orderService.getPointHistory(user.userId()));
    }

    // 마이포인트 조회
    @GetMapping("/points")
    public ResponseEntity<PointsResponseDTO> getMyPoints() {
    	UserInfoResponseDTO user = userService.getUserInfo();
    	return ResponseEntity.ok(new PointsResponseDTO(user.points()));
    }

    // 비밀번호 인증
    @PostMapping("/check/password")
    public ResponseEntity<PasswordCheckResponseDTO> verifyPassword(@Valid @RequestBody PasswordVerifyRequestDTO request) {
        boolean isValid = userService.verifyPassword(request.password());
        return ResponseEntity.ok(new PasswordCheckResponseDTO(
                isValid,
                isValid ? "비밀번호 인증에 성공했습니다." : "비밀번호가 일치하지 않습니다."
        ));
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<MessageResponseDTO> withdrawUser() {
    	userService.withdrawUser();
    	return ResponseEntity.ok(new MessageResponseDTO("회원 탈퇴가 성공적으로 처리되었습니다."));
    }
}