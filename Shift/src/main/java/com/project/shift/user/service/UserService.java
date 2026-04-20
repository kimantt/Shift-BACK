package com.project.shift.user.service;

import com.project.shift.chat.dao.ChatroomUserDAO;
import com.project.shift.chat.dao.FriendDAO;
import com.project.shift.global.exception.detail.UserConflictException;
import com.project.shift.global.exception.detail.UserNotFoundException;
import com.project.shift.global.exception.detail.UserValidationException;
import com.project.shift.global.exception.detail.UserWithdrawalNotAllowedException;
import com.project.shift.shop.dao.CartDAO;
import com.project.shift.shop.entity.Order;
import com.project.shift.shop.repository.DeliveryRepository;
import com.project.shift.shop.repository.OrderRepository;
import com.project.shift.user.dto.LoginIdRequestDTO;
import com.project.shift.user.dto.UserDTO;
import com.project.shift.user.entity.UserEntity;
import com.project.shift.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final String DELETED_USER_PREFIX = "deleted_";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartDAO cartDAO;
    private final FriendDAO friendDAO;
    private final ChatroomUserDAO chatroomUserDAO;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Long join(UserDTO userDTO) {
        validateName(userDTO);  //사용자 이름 검증
        validateTermsAgreement(userDTO); //약관 동의 검증

        UserEntity userEntity = convertToEntity(userDTO);
        UserEntity savedEntity = userRepository.save(userEntity);

        return savedEntity.getUserId();
    }

    //사용자 이름 검증
    private void validateName(UserDTO userDTO) {
        if (userDTO.name() == null || userDTO.name().trim().isEmpty()) {
            throw new UserValidationException("이름을 입력해야 합니다.");
        }

        if (userDTO.name().length() < 2 || userDTO.name().length() > 6) {
            throw new UserValidationException("이름은 2자 이상 6자 이하로 입력해야 합니다.");
        }

        if (!userDTO.name().matches("^[가-힣\\s]+$")) {
            throw new UserValidationException("이름은 한글만 사용할 수 있습니다.");
        }
    }

    // 아이디 중복 확인 - 사용 가능 여부 반환
    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new UserValidationException("아이디를 입력해주세요.");
        }

        if (loginId.length() < 4 || loginId.length() > 20) {
            throw new UserValidationException("아이디는 4자 이상 20자 이하로 설정해야 합니다.");
        }

        if (!loginId.matches("^[A-Za-z0-9]+$")) {
            throw new UserValidationException("아이디는 영문과 숫자만 사용할 수 있습니다.");
        }

        if (loginId.toLowerCase().startsWith("deleted")) {
            throw new UserValidationException("'deleted'로 시작하는 ID는 사용할 수 없습니다.");
        }

        return userRepository.existsByLoginId(loginId);
    }

    //약관 동의 검증
    private void validateTermsAgreement(UserDTO userDTO) {
        if (userDTO.termsAgreed() == null || !userDTO.termsAgreed()) {
            throw new UserValidationException("이용약관에 동의해야 합니다.");
        }
    }

    // 연락처 중복 확인 - 사용 가능 여부 반환
    public boolean isPhoneAvailable(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new UserValidationException("연락처를 입력해주세요.");
        }

        if (!phone.matches("^[0-9]{11}$")) {
            throw new UserValidationException("연락처는 11자리 숫자만 입력 가능합니다.");
        }

        return userRepository.existsByPhone(phone);
    }

    // 비밀번호 보안 규칙 검증
    public void validatePasswordRule(String password) {
        if (password == null || password.isEmpty()) {
            throw new UserValidationException("비밀번호를 입력해야 합니다.");
        }

        if (password.length() < 8 || password.length() > 24) {
            throw new UserValidationException("비밀번호는 8자 이상 24자 이하로 설정해야 합니다.");
        }

        if (!password.matches("^[A-Za-z0-9!@#$%^&*()]+$")) {
            throw new UserValidationException("비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.");
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()].*");

        if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
            throw new UserValidationException(
                    "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다.");
        }
    }

    //DTO를 Entity로 변환 및 암호화된 비밀번호 설정
    private UserEntity convertToEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .loginId(userDTO.loginId())
                .password(passwordEncoder.encode(userDTO.password()))
                .name(userDTO.name())
                .phone(userDTO.phone())
                .address(userDTO.address())
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
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

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
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        // 연락처 변경 시 중복 검증
        if (!userEntity.getPhone().equals(userDTO.phone())
                && userRepository.existsByPhone(userDTO.phone())) {
            throw new UserConflictException("이미 사용중인 연락처 입니다.");
        }

        //회원 정보 수정(Entity 업데이트)
        userEntity.updateInfo(userDTO.name(), userDTO.phone(), userDTO.address());

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

        UserEntity userEntity = userRepository.findByNameAndPhone(loginIdRequestDTO.name(), loginIdRequestDTO.phone())
                .orElseThrow(() -> new UserNotFoundException("일치하는 사용자가 없습니다."));

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
            throw new UserValidationException("[SYSTEM] 이름은 필수 입력 항목입니다.");
        }
        if (userFindDTO.phone() == null || userFindDTO.phone().isBlank()) {
            throw new UserValidationException("[SYSTEM] 연락처는 필수 입력 항목입니다.");
        }
    }

    // 비밀번호 인증
    @Transactional(readOnly = true)
    public boolean verifyPassword(String password) {
    	if (password == null || password.isBlank()) {
            throw new UserValidationException("비밀번호를 입력해주세요.");
        }
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long userId = Long.parseLong(auth.getName());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        return passwordEncoder.matches(password, user.getPassword());
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long userId = Long.parseLong(auth.getName());

        log.info("[USER] 회원 탈퇴 시작 {}", userId);

        // 결제 미완료/에러(P) 자동 삭제
        List<Order> errorOrders = orderRepository.findAllBySenderIdAndOrderStatus(userId, "P");
        if (!errorOrders.isEmpty()) {
            log.info("[탈퇴] 결제 미완료 주문 {}건 자동 삭제 처리", errorOrders.size());
            orderRepository.deleteAll(errorOrders);
        }

        // 진행 중인 주문이 있는지 확인
        boolean hasActiveOrders = deliveryRepository.existsByOrder_SenderIdAndDeliveryStatusIn(userId, List.of("S"));
        if (hasActiveOrders) {
            throw new UserWithdrawalNotAllowedException("현재 배송 중인 상품이 있어 탈퇴할 수 없습니다.\n상품이 도착하여 구매 확정(배송 완료)된 후 다시 시도해주세요.");
        }

        cartDAO.clearCartByUserId(userId); // 장바구니 비우기
        friendDAO.deleteAllFriends(userId); // 친구 관계 삭제
        chatroomUserDAO.deleteChatroomUsersByUserId(userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 회원 탈퇴 처리
        String deletedLoginId = DELETED_USER_PREFIX + user.getUserId();
        String discardedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        user.withdraw(deletedLoginId, discardedPassword, LocalDateTime.now());

        userRepository.save(user);

        // SecurityContext 초기화 (로그아웃 처리)
        SecurityContextHolder.clearContext();

        log.info("[USER] 회원 탈퇴 완료 {}", userId);
    }
}
