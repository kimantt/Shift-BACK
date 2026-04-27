package com.project.shift.chat.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.shift.chat.dto.request.MessageWithSenderDTO;
import com.project.shift.chat.dto.response.ChatroomDTO;
import com.project.shift.chat.dto.response.ChatroomListDTO;
import com.project.shift.chat.dto.response.MessageSearchResultDTO;
import com.project.shift.chat.service.ChatroomService;
import com.project.shift.chat.service.ChatroomUserService;
import com.project.shift.chat.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatroomController {
	
	private final ChatroomService chatroomService;
	private final ChatroomUserService chatroomUserService;
	private final MessageService messageService;
	
	// 사용자가 참여한 채팅방 목록 반환
	@GetMapping
	public List<ChatroomListDTO> getUserChatroomList(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
		List<ChatroomListDTO> chatroomList = chatroomService.getUserChatrooms(userId);
		return chatroomList;
	}
	
	// 특정 채팅방 반환
	@GetMapping("/{chatroomId}")
	public ResponseEntity<?> getChatroom(@PathVariable long chatroomId){
		try {
			Optional<ChatroomDTO> chatroomDTO = chatroomService.getChatroom(chatroomId);
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
	
	// 새로운 채팅방 추가 및 메시지 DB저장 & 브로드캐스팅
	@PostMapping
	public long addChatroom(@RequestBody MessageWithSenderDTO payload) {		
	    // 채팅방 생성 (Chatrooms) 및 생성된 채팅방 pk 반환
		// 내부 로직에 객체간 동일한 시간 설정 포함됨
		long newChatroomId = chatroomService.addChatroom(payload);
		// 새로 생성된 채팅방 pk MessageDTO에 세팅
		payload.getMessage().setChatroomId(newChatroomId);
		
		// 두 사용자의 새로운 채팅방 정보 추가 (ChatroomUsers)
	    chatroomUserService.addChatroomUsers(payload, newChatroomId);
	    
	    // 메시지 DB저장 & 브로드캐스팅
	    messageService.sendAndSaveMessage(payload.getMessage(), payload.getSender());
	    return newChatroomId;
	}
	
	// 특정 채팅방에 참여한 모든 사용자의 채팅방 삭제
	// → 실제 데이터 삭제가 아닌 pk, fk 빼고 초기화
	@DeleteMapping("/{chatroomId}") // 브로드캐스팅 추가 (채팅방 나감)
	public ResponseEntity<?> deleteChatroom(@PathVariable long chatroomId) {
	    try {
	        boolean deleted = chatroomService.deleteChatroomAndChatroomUsers(chatroomId);
	        if (deleted) {
	            return ResponseEntity.ok("Chatroom and ChatroomUsers deleted successfully");
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("Chatroom not found");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Error deleting chatroom: " + e.getMessage());
	    }
	}
	
	// 특정 채팅방에 참여한 일부 사용자 채팅방 삭제
	// → 실제 데이터 삭제가 아닌 pk, fk 빼고 초기화
	@DeleteMapping("/users/{chatroomUserId}") // 브로드캐스팅 추가 (채팅방 나감)
	public ResponseEntity<?> deleteUsersChatroom(@PathVariable long chatroomUserId) {
	    try {	    				
	        boolean deleted = chatroomUserService.deleteChatroomUser(chatroomUserId);
	        if (deleted) {
	            return ResponseEntity.ok("Chatroom deleted successfully");
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                                 .body("Chatroom not found");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Error deleting chatroom: " + e.getMessage());
	    }
	}
	
	/*
	 * ## 채팅 검색 ##
	 * 검색 방법에는 두 가지가 있음
	 * 1. 검색 키워드가 참여한 채팅 목록의 상대방 이름에 포함될 때
	 * 2. 검색 키워드가 참여한 채팅방의 메시지 내용에 포함될 때
	 * 
	 * 각각의 경우가 반환 타입이 다름
	 * 1. 기존의 채팅방 목록 형태와 일치
	 * 2. 기존의 채팅방 목록에 메시지 내용이 추가되고 채팅방의 최신 메시지와 최신 메시지 전송 시간이 빠짐
	 * 
	 * 사용자가 검색을 하면 두 API를 호출하면 되고 검색 결과 UI를 위, 아래로 나누어 보여주는 것을 고려하여 설계
	 */
	// 채팅방 검색 - 1. 검색 키워드가 참여한 채팅 목록의 상대방 이름에 포함될 때
	@GetMapping("/search/name")
	public ResponseEntity<?> searchChatroomUsersName(@RequestParam String input){
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Long userId = Long.parseLong(auth.getName());
			List<ChatroomListDTO> chatroomList = chatroomService.searchChatroomUsersName(input, userId);
			return ResponseEntity.ok(chatroomList);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Chatroom not found");
		}
	}
	
	// 채팅방 검색 - 2. 검색 키워드가 참여한 채팅방의 메시지 내용에 포함될 때
	@GetMapping("/search/messages")
	public ResponseEntity<?> searchChatroomMessages(@RequestParam String input){
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Long userId = Long.parseLong(auth.getName());
			List<MessageSearchResultDTO> messageList = chatroomService.searchChatroomMessages(input, userId);
			return ResponseEntity.ok(messageList);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Chatroom not found");
		}
	}

}
