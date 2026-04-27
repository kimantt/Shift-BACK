package com.project.shift.chat.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.request.DeletedChatroomUserInfoDTO;
import com.project.shift.chat.dto.request.MessageWithSenderDTO;
import com.project.shift.chat.dto.response.ChatroomListDTO;
import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.dto.response.projection.ChatroomListProjection;
import com.project.shift.chat.entity.ChatroomUserEntity;
import com.project.shift.chat.repository.ChatroomUserRepository;
import com.project.shift.chat.repository.MessageRepository;
import com.project.shift.global.exception.detail.user.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomUserService {

	private final ChatroomUserRepository chatroomUserRepository;
	private final MessageRepository messageRepository;
	
	// 특정 채팅방에 참여
	@Transactional
	public void addChatroomUsers(MessageWithSenderDTO dto, long chatroomId) {
		// 채팅 생성자 생성 후 저장
		ChatroomUserDTO sender = dto.getSender();		
		sender.setChatroomId(chatroomId);
		sender.setConnectionStatus("ON");
		chatroomUserRepository.save(ChatroomUserEntity.toEntity(sender));
		
		// 채팅 수신자 생성 후 저장
		ChatroomUserDTO receiver = ChatroomUserDTO.builder()
									.chatroomId(chatroomId)
									.connectionStatus("OF")
									.userId(dto.getReceiverId())
									.isDarkMode("N")
									.chatroomName(dto.getSenderName()+"님과의 채팅방")
									.createdTime(dto.getMessage().getSendDate())
									.lastConnectionTime(new Date(dto.getMessage().getSendDate().getTime() - 1000L)) // 안읽은 메시지 갯수 구하기 위해 1초 전으로 설정
									.build();
		chatroomUserRepository.save(ChatroomUserEntity.toEntity(receiver));
		return;
	}
	
	// 특정 채팅방에서 특정 사용자만 나가기 (사용자 key 보존, 상대방 데이터 보존)
	@Transactional
	public boolean deleteChatroomUser(long chatroomUserId) {
		// 삭제된(초기화된) 행이 있으면 true 반환
		if (chatroomUserRepository.existsById(chatroomUserId)) {
			chatroomUserRepository.initChatroomUserExceptKey(chatroomUserId);
			return true;
		}
		return false;
	}
	
	// 특정 채팅방에 참여한 모든 사용자 나가기 (생성된 key만 보존)
	@Transactional
	public boolean deleteAllChatroomUsers(long chatroomId) {
		// 삭제된(초기화된) 행이 있으면 true 반환
		if (chatroomUserRepository.existsByChatroomId(chatroomId)) {
			chatroomUserRepository.initAllChatroomUsersExceptKey(chatroomId);
			return true;
		}
		return false;
	}

	// 특정 채팅방 유저 정보 반환
	@Transactional(readOnly = true)
	public Optional<ChatroomUserDTO> getChatroomUser(long chatroomId, long userId) {
		Optional<ChatroomUserEntity> entityOpt = chatroomUserRepository.getChatroomUser(chatroomId, userId);
	    if (entityOpt.isEmpty()) {
	        throw new UserNotFoundException("특정 채팅방의 유저 정보가 없습니다.");
	    }

	    return entityOpt.map(ChatroomUserDTO::toDto);
	}
	
	@Transactional(readOnly = true)
	public Optional<ChatroomListDTO> getChatroomListView(long chatroomUserId, long userId){
		Optional<ChatroomListProjection> chatroomProjection = chatroomUserRepository.findChatroomByChatroomUserId(chatroomUserId);
		return chatroomProjection.map(p -> {
				   ChatroomListDTO dto = ChatroomListDTO.builder()
                	.chatroomUserId(p.getChatroomUserId())
                    .chatroomId(p.getChatroomId())
                    .chatroomName(p.getChatroomName())
                    .lastMsgContent(p.getLastMsgContent())
                    .lastMsgDate(toDate(p.getLastMsgDate()))
                    .lastConnectionTime(toDate(p.getLastConnectionTime()))
                    .createdTime(toDate(p.getCreatedTime()))
                    .connectionStatus(p.getConnectionStatus())
                    .isDarkMode(p.getIsDarkMode())
                    .receiverId(p.getReceiverId())
                    .receiverName(p.getReceiverName())
                    .build();

                // unreadCount 계산
                dto.setUnreadCount(messageRepository.countUnreadMessages(p.getChatroomId(), userId));

                return dto;
            });
	}
	
	// Date 세팅
	private Date toDate(java.sql.Timestamp ts) {
        return ts != null ? new Date(ts.getTime()) : null;
    }
	
	@Transactional(readOnly = true)
	public Optional<ChatroomUserDTO> getChatroomWithReceiver(long userId, long receiverId){
		List<Long> ids = new ArrayList<>();
		ids.add(userId);
		ids.add(receiverId);
		Optional<Long> chatroomIdOpt = chatroomUserRepository.findChatroomWithUsers(ids, ids.size());
		if (chatroomIdOpt.isEmpty()) { // 한 번도 채팅을 한 적이 없는 사용자들
			return Optional.empty();
		} else { // 채팅방 삭제 여부와 관계 없이 한 번이라도 채팅을 한 사용자들
			return getChatroomUser(chatroomIdOpt.get(), userId);
		}
	}
	
	// 채팅방 생성 시 두 사용자간 삭제된 채팅방 복구
	@Transactional
	public void restoreChatroomBetweenUsers(DeletedChatroomUserInfoDTO dto) {
		Date now = new Date();
		String senderChatroomName = dto.getReceiverName()+"님과의 채팅방";
		//String receiverChatroomName = dto.getSenderName()+"님과의 채팅방";
		
		chatroomUserRepository.restoreChatroomUser(dto.getChatroomId(), dto.getSenderId(), "ON", now, senderChatroomName);
		//chatroomUserDao.restoreChatroomUser(dto.getChatroomId(), dto.getReceiverId(), "OF", now, receiverChatroomName);
	}
	
	@Transactional
	public int updateChatroomName(ChatroomUserDTO dto) {
		return chatroomUserRepository.updateChatroomName(dto.getChatroomUserId(), dto.getChatroomName());
	}
}
