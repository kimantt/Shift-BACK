package com.project.shift.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.request.FriendDTO;
import com.project.shift.chat.dto.response.FriendInfoDTO;
import com.project.shift.chat.entity.FriendEntity;
import com.project.shift.chat.repository.FriendRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final FriendRepository friendRepository;
	
	@Transactional(readOnly = true)
	public List<FriendInfoDTO> getUserFriends(long userId){
		return friendRepository.getFriendsList(userId);
	}
	
	@Transactional
	public void addFriendship(FriendDTO dto) {
		friendRepository.save(FriendEntity.toEntity(dto));
		return;
	}
	
	@Transactional
	public boolean deleteFriend(long friendshipId) {
		// 삭제된 행이 있으면 true 반환
		if (friendRepository.existsById(friendshipId)) {
			friendRepository.deleteById(friendshipId);
			return true;
		}
		return false;
	}

}
