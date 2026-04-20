package com.project.shift.user.dto.response;

public record AvailabilityCheckResponseDTO(
		boolean available,
        String message
		) {

}
