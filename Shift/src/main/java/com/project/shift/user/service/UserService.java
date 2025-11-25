package com.project.shift.user.service;

import com.project.shift.user.dao.IUserDAO;
import com.project.shift.user.dto.UserDTO;
import com.project.shift.user.dto.LoginIdRequestDTO;
import com.project.shift.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final IUserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long join(UserDTO userDTO) {
        validateName(userDTO);  //사용자 이름 검증
        validateTermsAgreement(userDTO); //약관 동의 검증

        UserEntity userEntity = convertToEntity(userDTO);
        UserEntity savedEntity = userDAO.save(userEntity);

        return savedEntity.getUserId();
    }

    //사용자 이름 검증
    private void validateName(UserDTO userDTO) {
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }

        if (userDTO.getName().length() < 2 || userDTO.getName().length() > 6) {
            throw new IllegalArgumentException("이름은 2자 이상 6자 이하로 입력해야 합니다.");
        }

        if (!userDTO.getName().matches("^[가-힣\\s]+$")) {
            throw new IllegalArgumentException("이름은 한글만 사용할 수 있습니다.");
        }
    }

    // 아이디 중복 확인 - 사용 가능 여부 반환
    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (loginId.length() < 4 || loginId.length() > 20) {
            throw new IllegalArgumentException("아이디는 4자 이상 20자 이하로 설정해야 합니다.");
        }

        if (!loginId.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("아이디는 영문과 숫자만 사용할 수 있습니다.");
        }

        if (loginId.toLowerCase().startsWith("deleted")) {
            throw new IllegalArgumentException("'deleted'로 시작하는 ID는 사용할 수 없습니다.");
        }

        return userDAO.existsByLoginId(loginId);
    }

    //약관 동의 검증
    private void validateTermsAgreement(UserDTO userDTO) {
        if (userDTO.getTermsAgreed() == null || !userDTO.getTermsAgreed()) {
            throw new IllegalArgumentException("이용약관에 동의해야 합니다.");
        }
    }

    // 연락처 중복 확인 - 사용 가능 여부 반환
    public boolean isPhoneAvailable(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("연락처를 입력해주세요.");
        }

        if (!phone.matches("^[0-9]{11}$")) {
            throw new IllegalArgumentException("연락처는 11자리 숫자만 입력 가능합니다.");
        }

        return userDAO.existsByPhone(phone);
    }


    // 비밀번호 보안 규칙 검증
    public void validatePasswordRule(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }

        if (password.length() < 8 || password.length() > 24) {
            throw new IllegalArgumentException("비밀번호는 8자 이상 24자 이하로 설정해야 합니다.");
        }

        if (!password.matches("^[A-Za-z0-9!@#$%^&*()]+$")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.");
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()].*");

        if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
            throw new IllegalArgumentException(
                    "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다.");
        }
    }

    //DTO를 Entity로 변환 및 암호화된 비밀번호 설정
    private UserEntity convertToEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .loginId(userDTO.getLoginId())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .name(userDTO.getName())
                .phone(userDTO.getPhone())
                .address(userDTO.getAddress())
                .points(0)
                .adminFlag("N")
                .build();
    }

    // 로그인 ID로 본인 정보 조회
    @Transactional(readOnly = true)
    public UserDTO getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        //DB에서 회원 조회
        UserEntity userEntity = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        //비밀번호 제외하고 DTO로 변환하여 반환
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .loginId(userEntity.getLoginId())
                .name(userEntity.getName())
                .phone(userEntity.getPhone())
                .address(userEntity.getAddress())
                .points(userEntity.getPoints())
                .build();
    }

    // 로그인 ID로 본인 정보 수정
    @Transactional
    public UserDTO updateUserInfo(UserDTO userDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        //DB에서 회원 조회
        UserEntity userEntity = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 연락처 변경 시 중복 검증
        if (!userEntity.getPhone().equals(userDTO.getPhone())
                && userDAO.existsByPhone(userDTO.getPhone())) {
            throw new IllegalArgumentException("이미 사용중인 연락처 입니다.");
        }

        //회원 정보 수정(Entity 업데이트)
        userEntity.updateInfo(userDTO.getName(), userDTO.getPhone(), userDTO.getAddress());

        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .loginId(userEntity.getLoginId())
                .name(userEntity.getName())
                .phone(userEntity.getPhone())
                .address(userEntity.getAddress())
                .points(userEntity.getPoints())
                .build();
    }

    // 아이디 찾기
    @Transactional(readOnly = true)
    public String findId(LoginIdRequestDTO loginIdRequestDTO) {
        validateDTO(loginIdRequestDTO);

        UserEntity userEntity = userDAO.findByNameAndPhone(loginIdRequestDTO.name(), loginIdRequestDTO.phone())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자가 없습니다."));

        return maskLoginId(userEntity.getLoginId());
    }

    private String maskLoginId(String loginId) {
        // loginId의 반절만 마스킹 처리
        int length = loginId.length();
        int maskLength = length / 2;
        return loginId.substring(0, length - maskLength) +
                "*".repeat(maskLength);
    }

    private void validateDTO(LoginIdRequestDTO userFindDTO) {
        if (userFindDTO.name() == null || userFindDTO.name().isBlank()) {
            throw new IllegalArgumentException("[SYSTEM] 이름은 필수 입력 항목입니다.");
        }
        if (userFindDTO.phone() == null || userFindDTO.phone().isBlank()) {
            throw new IllegalArgumentException("[SYSTEM] 연락처는 필수 입력 항목입니다.");
        }
    }
}
