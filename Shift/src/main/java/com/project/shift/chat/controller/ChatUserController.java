package com.project.shift.chat.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.shift.chat.dto.response.ChatUserMyPageInfoDTO;
import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.service.ChatUserService;
import com.project.shift.chat.service.ChatroomUserService;
import com.project.shift.global.exception.detail.user.UserNotFoundException;
import com.project.shift.global.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat/users")
public class ChatUserController {

	private final ChatUserService chatUserService;
	private final ChatroomUserService chatroomUserService;
	private final JwtService jwtService;

	// 특정 채팅방 유저 정보 반환
	@GetMapping("/{chatroomId}")
	public ResponseEntity<?> getChatroomUser(HttpServletRequest request, @PathVariable long chatroomId){
		try {
			// jwt에서 현재 사용자의 토큰 추출
			String token = jwtService.extractTokenFromRequest(request);
			// 토큰에서 현재 사용자의 PK 추출
			long userId = jwtService.extractUserIdFromValidToken(token);
			
			Optional<ChatroomUserDTO> chatroomUserDTO = chatroomUserService.getChatroomUser(chatroomId, userId);
			if (chatroomUserDTO.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
		               .body("Chatroom not found");
		   } else {
				return ResponseEntity.ok(chatroomUserDTO);
		   }
		} catch (Exception e) {
		   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                        .body("Error searching chatroom: " + e.getMessage());
		}
	}

	// 전화번호로 사용자 검색 및 친구여부 반환
	@GetMapping("/search/{phone}")
	public ResponseEntity<?> searchUser(HttpServletRequest request, @PathVariable String phone) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        try {
        	return ResponseEntity.ok(chatUserService.searchUserByPhone(userId, phone));
        	
        } catch (UserNotFoundException e) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND)
    				.body("User not found");
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching user: " + e.getMessage());
        }
	}
	
	// 채팅-마이페이지 개인 정보(ID, 이름, 핸드폰 번호) 반환, 프로필 이미지는 추후 추가 예정
	@GetMapping("/me")
	public ResponseEntity<?> getChatUserInfo() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
		try {
			Optional<ChatUserMyPageInfoDTO> mypageDto = chatUserService.getChatUserInfo(userId);
			if (mypageDto.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Chatroom user not found");
	        } else {
				return ResponseEntity.ok(mypageDto);
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Error searching chatroom user: " + e.getMessage());
	    }
	}
	
	// 프로필 이미지 업로드
	@PostMapping("/uploadProfileImage")
	public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        
        try {
            chatUserService.uploadProfileImage(userId, file);
            return ResponseEntity.ok("프로필 이미지 업로드 완료");
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패 userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 이미지 업로드 실패: " + e.getMessage());
        }
	}
}