package com.project.shift.chat.controller;

import static com.project.shift.global.security.CurrentUser.getUserIdOrNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.shift.chat.dto.request.DeletedChatroomUserInfoDTO;
import com.project.shift.chat.dto.response.ChatroomListDTO;
import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.service.ChatroomUserService;
import com.project.shift.user.dto.response.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom/users")
public class ChatroomUserController {

	private final ChatroomUserService chatroomUserService;
	
	// 특정 두 유저가 참여한 채팅방 정보 확인 및 반환
	@GetMapping("/receiver/{receiverId}")
	public ResponseEntity<ChatroomUserDTO> getChatroomWithReceiver(@PathVariable long receiverId) {
        return ResponseEntity.ok(chatroomUserService.getChatroomWithReceiver(getUserIdOrNull(), receiverId));
    }
	
	// CHATROOM-08 : 특정 채팅방 정보 반환
	@GetMapping("/{chatroomUserId}")
	public ResponseEntity<ChatroomListDTO> getChatroomListView(@PathVariable long chatroomUserId) {
        return ResponseEntity.ok(chatroomUserService.getChatroomListView(chatroomUserId, getUserIdOrNull()));
    }
	
	// 채팅방 생성 시 두 사용자간 삭제된 채팅방 복구
	@PostMapping("/restore")
	public ResponseEntity<MessageResponseDTO> restoreChatroomBetweenUsers(@RequestBody DeletedChatroomUserInfoDTO dto) {
        chatroomUserService.restoreChatroomBetweenUsers(dto);
        return ResponseEntity.ok(new MessageResponseDTO("삭제된 채팅방이 복구되었습니다."));
    }
	
	// 채팅방 이름 변경
	@PatchMapping("/chatroom-name")
	public ResponseEntity<MessageResponseDTO> updateChatroomName(@RequestBody ChatroomUserDTO dto) {
        chatroomUserService.updateChatroomName(dto);
        return ResponseEntity.ok(new MessageResponseDTO("채팅방 이름이 변경되었습니다."));
    }
}
