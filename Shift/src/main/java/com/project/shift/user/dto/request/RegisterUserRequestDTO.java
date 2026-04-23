package com.project.shift.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterUserRequestDTO(
		@NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 설정해야 합니다.")
		@Pattern.List({
            @Pattern(regexp = "^[A-Za-z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다."),
            @Pattern(regexp = "^(?!deleted).*$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "'deleted'로 시작하는 ID는 사용할 수 없습니다.")
		})
        String loginId,

        @NotBlank(message = "비밀번호를 입력해야 합니다.")
        @Size(min = 8, max = 24, message = "비밀번호는 8자 이상 24자 이하로 설정해야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9!@#$%^&*()]+$", message = "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.")
        String password,

        @NotBlank(message = "이름을 입력해야 합니다.")
        @Size(min = 2, max = 6, message = "이름은 2자 이상 6자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^[가-힣\\s]+$", message = "이름은 한글만 사용할 수 있습니다.")
        String name,

        @NotBlank(message = "연락처를 입력해주세요.")
        @Pattern(regexp = "^[0-9]{11}$", message = "연락처는 11자리 숫자만 입력 가능합니다.")
        String phone,

        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @NotNull(message = "이용약관 동의 여부를 입력해야 합니다.")
        @AssertTrue(message = "이용약관에 동의해야 합니다.")
        Boolean termsAgreed
		) {

}
