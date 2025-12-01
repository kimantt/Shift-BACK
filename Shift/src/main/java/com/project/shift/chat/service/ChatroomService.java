package com.project.shift.chat.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dao.ChatroomDAO;
import com.project.shift.chat.dto.ChatroomDTO;
import com.project.shift.chat.dto.ChatroomListDTO;
import com.project.shift.chat.dto.ChatroomListProjection;
import com.project.shift.chat.dto.MessageSearchResultDTO;
import com.project.shift.chat.dto.MessageSearchResultProjection;
import com.project.shift.chat.dto.MessageWithSenderDTO;
import com.project.shift.chat.entity.ChatroomEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatroomService {

	private final ChatroomDAO dao;
	private final ChatroomUserService chatroomUserService;
	
	// 특정 채팅방 정보 반환
	@Transactional(readOnly = true)
	public Optional<ChatroomDTO> getChatroom(long chatroomId) {
		return dao.findChatroomById(chatroomId)
	              .map(ChatroomDTO::toDto);
	}
	
	// 채팅방 검색 - 1. 검색 키워드가 참여한 채팅 목록의 상대방 이름에 포함될 때
	@Transactional(readOnly = true)
	public List<ChatroomListDTO> searchChatroomUsersName(String input, long userId) {
		List<ChatroomListProjection> chatroomList = dao.searchChatroomUsersName(input, userId);
		return chatroomListDTOBuilder(chatroomList, userId);
	}
	
	// 채팅 검색 - 채팅 메시지 검색
	@Transactional(readOnly = true)
	public List<MessageSearchResultDTO> searchChatroomMessages(String input, long userId){
		List<MessageSearchResultProjection> chatroomList = dao.searchChatroomMessages(input, userId);
		return MessageSearchResultDTOBuilder(chatroomList, userId);
	}
		
	// 사용자가 참여한 채팅방 목록 반환
	@Transactional(readOnly = true)
	public List<ChatroomListDTO> getUserChatrooms(long userId){
		List<ChatroomListProjection> chatroomList = dao.getUserChatrooms(userId);
		return chatroomListDTOBuilder(chatroomList, userId);
	}
	
	// Date 세팅
	private Date toDate(java.sql.Timestamp ts) {
        return ts != null ? new Date(ts.getTime()) : null;
    }
	
	@Transactional
	public long addChatroom(MessageWithSenderDTO dto) {
		// 채팅방을 만들면서 전송한 메시지를 이용해서 채팅방 생성
		ChatroomDTO newChatroom = new ChatroomDTO();
		// 채팅방 생성 시간, 메시지 전송 시간, 채팅방에 전송된 최신 메시지 전송 시간 동일하게 세팅
		setTimestamps(dto, newChatroom);
		newChatroom.setLastMsgContent(dto.getMessage().getContent());
				
		// 저장 후 DB에서 생성된 PK 가져오기
		ChatroomEntity entity = ChatroomEntity.toEntity(newChatroom);
	    ChatroomEntity savedEntity = dao.saveChatroom(entity);
	    log.info("[CHATRROM] 채팅방 생성 완료");
	    return savedEntity.getChatroomId();
	}
	
	// 특정 채팅방에 참여한 모든 사용자, 특정 채팅방 정보 전체 삭제됨
	// → pk,fk 제외 모든 데이터 초기화
	@Transactional
	public boolean deleteChatroomAndChatroomUsers(long chatroomId) {
		// Chatroom 초기화
		boolean ifChatroomDeleted = dao.initChatroomExceptKey(chatroomId);
		if (ifChatroomDeleted) {
			// ChatroomUsers 초기화
			return chatroomUserService.deleteAllChatroomUsers(chatroomId);
		}
		return false;
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
	
	private List<ChatroomListDTO> chatroomListDTOBuilder(List<ChatroomListProjection> chatroomList, long userId){
		return chatroomList.stream().map(p -> {
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
                .build();
            // unreadCount 계산
            dto.setUnreadCount(dao.countUnreadMessages(p.getChatroomId(), userId));
            
            return dto;
        }).toList();
	}
	
	private List<MessageSearchResultDTO> MessageSearchResultDTOBuilder(List<MessageSearchResultProjection> chatroomList,
																		long userId){
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
                .build();
           // unreadCount 계산
           dto.setUnreadCount(dao.countUnreadMessages(p.getChatroomId(), userId));
           return dto;
     }).toList();
	}

}
