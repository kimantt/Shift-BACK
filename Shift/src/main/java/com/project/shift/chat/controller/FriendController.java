package com.project.shift.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.shift.chat.dto.request.FriendDTO;
import com.project.shift.chat.dto.response.FriendInfoDTO;
import com.project.shift.chat.service.FriendService;
import com.project.shift.user.dto.response.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {
	
	private final FriendService friendService;
	
	// 친구 목록 조회
	@GetMapping("/users/{userId}")
	public List<FriendInfoDTO> getFriendList(@PathVariable long userId) {
        return friendService.getUserFriends(userId);
    }
	
	// 친구 추가
	@PostMapping
	public MessageResponseDTO addFriendship(@RequestBody FriendDTO friendInfo) {
        friendService.addFriendship(friendInfo);
        return new MessageResponseDTO("친구가 추가되었습니다.");
    }
	
	// 친구 삭제
	@DeleteMapping("/{friendshipId}")
	public MessageResponseDTO deleteFriend(@PathVariable long friendshipId) {
        friendService.deleteFriend(friendshipId);
        return new MessageResponseDTO("친구가 삭제되었습니다.");
    }
}