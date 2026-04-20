package com.project.shift.user.dto.request;

import lombok.Builder;

@Builder
public record RegisterUserRequestDTO(
		String loginId,
        String password,
        String name,
        String phone,
        String address,
        Boolean termsAgreed
		) {

}
