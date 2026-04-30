package com.project.shift.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.request.FriendDTO;
import com.project.shift.chat.dto.response.FriendInfoDTO;
import com.project.shift.chat.entity.FriendEntity;
import com.project.shift.chat.repository.ChatUserRepository;
import com.project.shift.chat.repository.FriendRepository;
import com.project.shift.global.exception.detail.user.UserNotFoundException;
import com.project.shift.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final FriendRepository friendRepository;
	private final ChatUserRepository chatUserRepository;
	
	@Transactional(readOnly = true)
	public List<FriendInfoDTO> getUserFriends(long userId) {
        return friendRepository.getFriendsList(userId);
    }
	
	@Transactional
	public void addFriendship(FriendDTO dto) {
		UserEntity user = chatUserRepository.getReferenceById(dto.getUserId());
        UserEntity friend = chatUserRepository.getReferenceById(dto.getFriendId());
        friendRepository.save(FriendEntity.from(dto, user, friend));
    }
	
	@Transactional
	public void deleteFriend(long friendshipId) {
        if (!friendRepository.existsById(friendshipId)) {
            throw new UserNotFoundException("친구 관계를 찾을 수 없습니다.");
        }
        friendRepository.deleteById(friendshipId);
    }
}
