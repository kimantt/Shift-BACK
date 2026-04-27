package com.project.shift.chat.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom/users")
public class ChatroomUserController {

	private final ChatroomUserService chatroomUserService;
	
	// 특정 두 유저가 참여한 채팅방 정보 확인 및 반환
	@GetMapping("/receiver/{receiverId}")
	public ResponseEntity<?> getChatroomWithReceiver(HttpServletRequest request, @PathVariable long receiverId) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        Long userId = Long.parseLong(auth.getName());
			Optional<ChatroomUserDTO> chatroomUserDTO = chatroomUserService.getChatroomWithReceiver(userId, receiverId);
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
	
	
	// CHATROOM-08 : 특정 채팅방 정보 반환
	@GetMapping("/{chatroomUserId}")
	public ResponseEntity<?> getChatroomListView(@PathVariable long chatroomUserId){
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        Long userId = Long.parseLong(auth.getName());
			Optional<ChatroomListDTO> chatroomDTO = chatroomUserService.getChatroomListView(chatroomUserId, userId);
			if (chatroomDTO.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Chatroom not found");
	        } else {
				return ResponseEntity.ok(chatroomDTO);
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Error searching chatroom: " + e.getMessage());
	    }
	}
	
	// 채팅방 생성 시 두 사용자간 삭제된 채팅방 복구
	@PostMapping("/restore")
	public void restoreChatroomBetweenUsers(@RequestBody DeletedChatroomUserInfoDTO dto){
		chatroomUserService.restoreChatroomBetweenUsers(dto);
	}
	
	// 채팅방 이름 변경
	@PatchMapping("/chatroom-name")
	public ResponseEntity<?> updateChatroomName(@RequestBody ChatroomUserDTO dto) {
		try {
			int updated = chatroomUserService.updateChatroomName(dto);
			if (updated > 0) {
				return ResponseEntity.ok("Chatroom name successfully updated!");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
		               .body("Chatroom not found");
			}
		} catch (Exception e) {
			   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body("Error updating chatroom name: " + e.getMessage());
		}
	}

}
