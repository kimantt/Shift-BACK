package com.project.shift.chat.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.request.MessageWithSenderDTO;
import com.project.shift.chat.dto.response.ChatroomDTO;
import com.project.shift.chat.dto.response.ChatroomListDTO;
import com.project.shift.chat.dto.response.MessageSearchResultDTO;
import com.project.shift.chat.dto.response.projection.ChatroomListProjection;
import com.project.shift.chat.dto.response.projection.MessageSearchResultProjection;
import com.project.shift.chat.entity.ChatroomEntity;
import com.project.shift.chat.repository.ChatroomRepository;
import com.project.shift.chat.repository.MessageRepository;
import com.project.shift.global.exception.detail.user.UserNotFoundException;
import com.project.shift.global.exception.detail.user.UserValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomService {

	private final ChatroomRepository chatroomRepository;
	private final MessageRepository messageRepository;
	private final ChatroomUserService chatroomUserService;
	
	// 특정 채팅방 정보 반환
	@Transactional(readOnly = true)
	public ChatroomDTO getChatroom(long chatroomId) {
        return chatroomRepository.findById(chatroomId)
                .map(ChatroomDTO::toDto)
                .orElseThrow(() -> new UserNotFoundException("채팅방을 찾을 수 없습니다."));
    }
	
	// 채팅방 검색 - 1. 검색 키워드가 참여한 채팅 목록의 상대방 이름에 포함될 때
	@Transactional(readOnly = true)
	public List<ChatroomListDTO> searchChatroomUsersName(String input, long userId) {
        validateSearchInput(input);
        List<ChatroomListProjection> chatroomList = chatroomRepository.findChatroomUsersBySearchInput(input, userId);
        return buildChatroomList(chatroomList, userId);
    }
	
	// 채팅 검색 - 채팅 메시지 검색
	@Transactional(readOnly = true)
	public List<MessageSearchResultDTO> searchChatroomMessages(String input, long userId) {
        validateSearchInput(input);
        List<MessageSearchResultProjection> chatroomList = chatroomRepository.findChatroomMessagesBySearchInput(input, userId);
        return buildMessageSearchResults(chatroomList, userId);
    }
		
	// 사용자가 참여한 채팅방 목록 반환
	@Transactional(readOnly = true)
	public List<ChatroomListDTO> getUserChatrooms(long userId) {
        List<ChatroomListProjection> chatroomList = chatroomRepository.findChatroomsByUserId(userId);
        return buildChatroomList(chatroomList, userId);
    }
	
	@Transactional
	public long addChatroom(MessageWithSenderDTO dto) {
		// 채팅방을 만들면서 전송한 메시지를 이용해서 채팅방 생성
		ChatroomDTO newChatroom = new ChatroomDTO();
		// 채팅방 생성 시간, 메시지 전송 시간, 채팅방에 전송된 최신 메시지 전송 시간 동일하게 세팅
		setTimestamps(dto, newChatroom);
		newChatroom.setLastMsgContent(dto.getMessage().getContent());
				
		// 저장 후 DB에서 생성된 PK 가져오기
		ChatroomEntity savedEntity = chatroomRepository.save(ChatroomEntity.builder()
				.lastMsgContent(newChatroom.getLastMsgContent())
				.lastMsgDate(newChatroom.getLastMsgDate())
				.build());
	    return savedEntity.getChatroomId();
	}
	
	// 특정 채팅방에 참여한 모든 사용자, 특정 채팅방 정보 전체 삭제됨
	// → pk,fk 제외 모든 데이터 초기화
	@Transactional
	public void deleteChatroomAndChatroomUsers(long chatroomId) {
        if (!chatroomRepository.existsById(chatroomId)) {
            throw new UserNotFoundException("채팅방을 찾을 수 없습니다.");
        }

        chatroomRepository.initChatroomExceptKey(chatroomId);
        chatroomUserService.deleteAllChatroomUsers(chatroomId);
    }
	
	// 채팅방 생성 시간, 메시지 전송 시간, 채팅방에 전송된 최신 메시지 전송 시간 동일하게 세팅
	private void setTimestamps(MessageWithSenderDTO payload, ChatroomDTO chatroomDTO) {
        Date now = new Date();
        payload.getMessage().setSendDate(now);
        payload.getSender().setCreatedTime(now);
        chatroomDTO.setLastMsgDate(now);
        // 채팅방 최초 생성시 해당 시점 이후의 채팅을 읽음처리 하기 위한 기준
        payload.getSender().setLastConnectionTime(now);
    }
	
	private List<ChatroomListDTO> buildChatroomList(List<ChatroomListProjection> chatroomList, long userId) {
        return chatroomList.stream().map(p -> {
            ChatroomListDTO dto = ChatroomListDTO.builder()
                    .chatroomUserId(p.getChatroomUserId())
                    .chatroomId(p.getChatroomId())
                    .chatroomName(p.getChatroomName())
                    .lastMsgSender(p.getLastMsgSender())
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
        }).toList();
    }
	
	private List<MessageSearchResultDTO> buildMessageSearchResults(List<MessageSearchResultProjection> chatroomList,
		            											   long userId) {
		return chatroomList.stream().map(p -> {
			MessageSearchResultDTO dto = MessageSearchResultDTO.builder()
					.chatroomUserId(p.getChatroomUserId())
					.chatroomId(p.getChatroomId())
					.chatroomName(p.getChatroomName())
					.lastConnectionTime(toDate(p.getLastConnectionTime()))
					.createdTime(toDate(p.getCreatedTime()))
					.connectionStatus(p.getConnectionStatus())
					.isDarkMode(p.getIsDarkMode())
					.message(p.getMessage())
					.sendDate(p.getSendDate())
					.receiverId(p.getReceiverId())
					.build();
		
			// unreadCount 계산
			dto.setUnreadCount(messageRepository.countUnreadMessages(p.getChatroomId(), userId));
			return dto;
		}).toList();
	}
	
	private void validateSearchInput(String input) {
        if (input == null || input.isBlank()) {
            throw new UserValidationException("검색어를 입력해 주세요.");
        }
    }

	// Date 세팅
    private Date toDate(Timestamp ts) {
        return ts != null ? new Date(ts.getTime()) : null;
    }
}
