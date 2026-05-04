package com.project.shift.chat.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.request.DeletedChatroomUserInfoDTO;
import com.project.shift.chat.dto.request.MessageWithSenderDTO;
import com.project.shift.chat.dto.response.ChatroomListDTO;
import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.dto.response.projection.ChatroomListProjection;
import com.project.shift.chat.entity.ChatroomEntity;
import com.project.shift.chat.entity.ChatroomUserEntity;
import com.project.shift.chat.repository.ChatUserRepository;
import com.project.shift.chat.repository.ChatroomRepository;
import com.project.shift.chat.repository.ChatroomUserRepository;
import com.project.shift.chat.repository.MessageRepository;
import com.project.shift.global.exception.detail.user.UserNotFoundException;
import com.project.shift.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomUserService {

	private final ChatroomUserRepository chatroomUserRepository;
	private final MessageRepository messageRepository;
	private final ChatroomRepository chatroomRepository;
	private final ChatUserRepository chatUserRepository;
	
	// 특정 채팅방에 참여
	@Transactional
	public void addChatroomUsers(MessageWithSenderDTO dto, long chatroomId) {
		// 채팅 생성자 생성 후 저장
		ChatroomUserDTO sender = dto.getSender();
        sender.setChatroomId(chatroomId);
        sender.setConnectionStatus("ON");
        ChatroomEntity chatroom = chatroomRepository.getReferenceById(chatroomId);
        UserEntity senderUser = chatUserRepository.getReferenceById(sender.getUserId());
        chatroomUserRepository.save(ChatroomUserEntity.from(sender, chatroom, senderUser));
		
		// 채팅 수신자 생성 후 저장
        ChatroomUserDTO receiver = ChatroomUserDTO.builder()
                .chatroomId(chatroomId)
                .connectionStatus("OF")
                .userId(dto.getReceiverId())
                .isDarkMode("N")
                .chatroomName(dto.getSenderName() + "님과의 채팅방")
                .createdTime(dto.getMessage().getSendDate())
                .lastConnectionTime(new Date(dto.getMessage().getSendDate().getTime() - 1000L))
                .build();
        UserEntity receiverUser = chatUserRepository.getReferenceById(receiver.getUserId());
        chatroomUserRepository.save(ChatroomUserEntity.from(receiver, chatroom, receiverUser));
	}
	
	// 특정 채팅방에서 특정 사용자만 나가기 (사용자 key 보존, 상대방 데이터 보존)
	@Transactional
	public void deleteChatroomUser(long chatroomUserId) {
		if (!chatroomUserRepository.existsById(chatroomUserId)) {
            throw new UserNotFoundException("채팅방 참여 정보를 찾을 수 없습니다.");
        }
        chatroomUserRepository.initChatroomUserExceptKey(chatroomUserId);
	}

	// 특정 채팅방 유저 정보 반환
	@Transactional(readOnly = true)
	public ChatroomUserDTO getChatroomUser(long chatroomId, long userId) {
        return chatroomUserRepository.getChatroomUser(chatroomId, userId)
                .map(ChatroomUserDTO::toDto)
                .orElseThrow(() -> new UserNotFoundException("특정 채팅방의 유저 정보가 없습니다."));
    }
	
	@Transactional(readOnly = true)
	public ChatroomListDTO getChatroomListView(long chatroomUserId, long userId) {
        ChatroomListProjection projection = chatroomUserRepository.findChatroomByChatroomUserId(chatroomUserId)
                .orElseThrow(() -> new UserNotFoundException("채팅방 목록 정보를 찾을 수 없습니다."));

        ChatroomListDTO dto = ChatroomListDTO.builder()
                .chatroomUserId(projection.getChatroomUserId())
                .chatroomId(projection.getChatroomId())
                .chatroomName(projection.getChatroomName())
                .lastMsgContent(projection.getLastMsgContent())
                .lastMsgDate(toDate(projection.getLastMsgDate()))
                .lastConnectionTime(toDate(projection.getLastConnectionTime()))
                .createdTime(toDate(projection.getCreatedTime()))
                .connectionStatus(projection.getConnectionStatus())
                .isDarkMode(projection.getIsDarkMode())
                .receiverId(projection.getReceiverId())
                .receiverName(projection.getReceiverName())
                .build();

        // unreadCount 계산
        dto.setUnreadCount(messageRepository.countUnreadMessages(projection.getChatroomId(), userId));
        return dto;
    }
	
	@Transactional(readOnly = true)
	public ChatroomUserDTO getChatroomWithReceiver(long userId, long receiverId) {
        List<Long> ids = new ArrayList<>();
        ids.add(userId);
        ids.add(receiverId);

        Long chatroomId = chatroomUserRepository.findChatroomWithUsers(ids, ids.size())
                .orElseThrow(() -> new UserNotFoundException("두 사용자 간 채팅방을 찾을 수 없습니다."));

        return getChatroomUser(chatroomId, userId);
    }
	
	// 채팅방 생성 시 두 사용자간 삭제된 채팅방 복구
	@Transactional
	public void restoreChatroomBetweenUsers(DeletedChatroomUserInfoDTO dto) {
        Date now = new Date();
        String senderChatroomName = dto.getReceiverName() + "님과의 채팅방";
        chatroomUserRepository.restoreChatroomUser(dto.getChatroomId(), dto.getSenderId(), "ON", now, senderChatroomName);
    }
	
	@Transactional
	public void updateChatroomName(ChatroomUserDTO dto) {
        int updated = chatroomUserRepository.updateChatroomName(dto.getChatroomUserId(), dto.getChatroomName());
        if (updated <= 0) {
            throw new UserNotFoundException("채팅방 정보를 찾을 수 없습니다.");
        }
    }
	
	private Date toDate(Timestamp ts) {
        return ts != null ? new Date(ts.getTime()) : null;
    }
}
