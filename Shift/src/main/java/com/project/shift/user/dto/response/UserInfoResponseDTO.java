package com.project.shift.user.dto.response;

import lombok.Builder;

@Builder
public record UserInfoResponseDTO(
		Long userId,
        String loginId,
        String name,
        String phone,
        String address,
        Integer points
		) {

}
