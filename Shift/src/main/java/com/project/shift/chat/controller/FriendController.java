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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {
	
	private final FriendService friendService;
	
	// 친구 목록 조회
	@GetMapping("/users/{userId}")
	public List<FriendInfoDTO> getFriendList(@PathVariable long userId){
		return friendService.getUserFriends(userId);
	}
	
	// 친구 추가
	@PostMapping
	public void addFriendship(@RequestBody FriendDTO friendInfo) {
		friendService.addFriendship(friendInfo);
		return;
	}
	
	// 친구 삭제
	@DeleteMapping("/{friendshipId}")
	public void deleteFriend(@PathVariable long friendshipId) {
		// 친구 삭제
		friendService.deleteFriend(friendshipId);
	}
	
}