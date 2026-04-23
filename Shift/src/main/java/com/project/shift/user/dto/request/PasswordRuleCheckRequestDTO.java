package com.project.shift.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordRuleCheckRequestDTO(
		@NotBlank(message = "비밀번호를 입력해야 합니다.")
        @Size(min = 8, max = 24, message = "비밀번호는 8자 이상 24자 이하로 설정해야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9!@#$%^&*()]+$", message = "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다.")
        String password
		) {

}
