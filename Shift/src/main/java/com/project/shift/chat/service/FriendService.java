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
import com.project.shift.global.exception.detail.user.UserValidationException;
import com.project.shift.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final FriendRepository friendRepository;
	private final ChatUserRepository chatUserRepository;
	
	@Transactional(readOnly = true)
	public List<FriendInfoDTO> getUserFriends(long userId) {
		validateUserId(userId);
        return friendRepository.getFriendsList(userId);
    }
	
	@Transactional
	public void addFriendship(FriendDTO dto) {
		validateFriendRequest(dto);

        if (friendRepository.existsByUser_UserIdAndFriend_UserId(dto.getUserId(), dto.getFriendId())) {
            throw new UserValidationException("이미 등록된 친구입니다.");
        }
		
        UserEntity user = chatUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        UserEntity friend = chatUserRepository.findById(dto.getFriendId())
                .orElseThrow(() -> new UserNotFoundException("친구 사용자를 찾을 수 없습니다."));
        
        friendRepository.save(FriendEntity.of(dto.getFriendshipId(), user, friend));
    }
	
	@Transactional
	public void deleteFriend(long friendshipId) {
        if (!friendRepository.existsById(friendshipId)) {
            throw new UserNotFoundException("친구 관계를 찾을 수 없습니다.");
        }
        friendRepository.deleteById(friendshipId);
    }
	
	private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new UserValidationException("유효하지 않은 사용자입니다.");
        }
    }
	
	private void validateFriendRequest(FriendDTO dto) {
        if (dto == null) {
            throw new UserValidationException("친구 요청 정보가 없습니다.");
        }
        if (dto.getUserId() <= 0 || dto.getFriendId() <= 0) {
            throw new UserValidationException("유효하지 않은 사용자 정보입니다.");
        }
        if (dto.getUserId() == dto.getFriendId()) {
            throw new UserValidationException("자기 자신을 친구로 추가할 수 없습니다.");
        }
    }
}
