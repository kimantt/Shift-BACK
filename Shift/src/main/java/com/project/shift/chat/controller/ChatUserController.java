package com.project.shift.chat.controller;

import static com.project.shift.global.security.CurrentUser.getUserIdOrNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.shift.chat.dto.response.ChatUserMyPageInfoDTO;
import com.project.shift.chat.dto.response.ChatUserSearchResultDTO;
import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.service.ChatUserService;
import com.project.shift.chat.service.ChatroomUserService;
import com.project.shift.user.dto.response.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/users")
public class ChatUserController {

	private final ChatUserService chatUserService;
	private final ChatroomUserService chatroomUserService;

	// 특정 채팅방 유저 정보 반환
	@GetMapping("/{chatroomId}")
	public ResponseEntity<ChatroomUserDTO> getChatroomUser(@PathVariable long chatroomId){
		ChatroomUserDTO chatroomUserDTO = chatroomUserService.getChatroomUser(chatroomId, getUserIdOrNull());
        return ResponseEntity.ok(chatroomUserDTO);
	}

	// 전화번호로 사용자 검색 및 친구여부 반환
	@GetMapping("/search/{phone}")
	public ResponseEntity<ChatUserSearchResultDTO> searchUser(@PathVariable String phone) {
        ChatUserSearchResultDTO response = chatUserService.searchUserByPhone(getUserIdOrNull(), phone);
        return ResponseEntity.ok(response);
    }
	
	// 채팅-마이페이지 개인정보 반환
	@GetMapping("/me")
	public ResponseEntity<ChatUserMyPageInfoDTO> getChatUserInfo() {
        return ResponseEntity.ok(chatUserService.getChatUserInfo(getUserIdOrNull()));
    }
	
	// 프로필 이미지 업로드
	@PostMapping("/uploadProfileImage")
	public ResponseEntity<MessageResponseDTO> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        chatUserService.uploadProfileImage(getUserIdOrNull(), file);
        return ResponseEntity.ok(new MessageResponseDTO("프로필 이미지 업로드 완료"));
    }
}