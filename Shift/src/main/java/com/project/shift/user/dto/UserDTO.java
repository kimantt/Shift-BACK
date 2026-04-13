package com.project.shift.user.dto;

import lombok.Builder;

@Builder
public record UserDTO(
		Long userId,
	    String loginId,
	    String password,
	    String name,
	    String phone,
	    String address,
	    Integer points,
	    Boolean termsAgreed
		) {
	
}