package com.project.shift.user.dto.response;

public record PasswordCheckResponseDTO(
		boolean valid,
        String message
		) {

}
