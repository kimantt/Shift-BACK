package com.project.shift.user.dto.request;

public record UpdateUserInfoRequestDTO(
		String name,
        String phone,
        String address
		) {

}
