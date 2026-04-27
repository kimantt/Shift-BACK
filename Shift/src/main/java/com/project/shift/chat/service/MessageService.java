package com.project.shift.chat.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.shift.chat.dto.ChatroomDTO;
import com.project.shift.chat.dto.ChatroomListDTO;
import com.project.shift.chat.dto.ChatroomUserDTO;
import com.project.shift.chat.dto.MessageDTO;
import com.project.shift.chat.dto.MessageUserDTO;
import com.project.shift.chat.entity.ChatUserEntity;
import com.project.shift.chat.entity.MessageEntity;
import com.project.shift.chat.repository.ChatUserRepository;
import com.project.shift.chat.repository.ChatroomRepository;
import com.project.shift.chat.repository.ChatroomUserRepository;
import com.project.shift.chat.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
	
	private final MessageRepository messageRepository;
	private final ChatroomUserRepository chatroomUserRepository;
	private final ChatroomRepository chatroomRepository;
	private final ChatUserRepository chatUserRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 메시지 DB 저장
	@Transactional
	public void addMessage(MessageDTO message) {
		messageRepository.save(MessageEntity.toEntity(message));
	}
	
	// 채팅방 최초 접속 시간 이후 모든 채팅방 메시지 반환
	@Transactional(readOnly = true)
	public List<MessageDTO> getMessageHistory(ChatroomListDTO dto){
		long chatroomId = dto.getChatroomId();
		Date createdDateTime = dto.getCreatedTime();
		List<MessageEntity> entityList = messageRepository.findByChatroomId(chatroomId, createdDateTime);
		List<MessageDTO> dtoList = new ArrayList<MessageDTO>();
		for (MessageEntity e : entityList) {
			dtoList.add(MessageDTO.toDto(e));
		}
		return dtoList;
	}
	
	// 채팅 메시지 전송시 메시지 DB에 저장 및 브로드캐스팅
	@Transactional
	public void sendAndSaveMessage(MessageDTO messageDTO, ChatroomUserDTO chatroomUserDTO) {
		// 채팅을 보내는 사람이 이전에 채팅방을 삭제한 적이 있는 경우 채팅방 생성시간과 마지막 접속 시간을 메시지를 보내기 직전으로 설정
		if (chatroomUserDTO.getConnectionStatus().equals("DL")) {
			Date now = new Date();
			chatroomUserDTO.setLastConnectionTime(now);
			
			long receiverId = chatroomUserRepository.getReceiverId(chatroomUserDTO.getChatroomId(), chatroomUserDTO.getUserId()).getFirst();
			Optional<ChatUserEntity> receiverInfo = chatUserRepository.findById(receiverId);
			
			chatroomUserRepository.restoreChatroomUser(chatroomUserDTO.getChatroomId(),
												chatroomUserDTO.getUserId(),
												"OF",
												now,
												receiverInfo.get().getName() + "님과의 채팅방");
		}
		
		// 채팅방을 완전히 처음 생성해서 메시지를 보내는 경우 메시지 전송 시간이 채팅방 생성시간과 동일하게 설정되어있음
		// 채팅방에 참여한 후 메시지 전송시, 삭제된 채팅방 복구시 메시지 전송시간이 아직 null인 상태
		if (messageDTO.getSendDate() == null) {
			messageDTO.setSendDate(new Date());
		}
		
		// 메시지 타입에 따라 필드값 변경 및 DB에 저장
		switch (messageDTO.getType()) {
	        case JOIN :
	        	// 접속 상태 ON으로 세팅
	        	chatroomUserDTO.setConnectionStatus("ON");
	        	// 채팅방 마지막 접속 시간 이후 모든 메시지 읽음 처리
	        	messageRepository.markMessagesAsRead(messageDTO.getChatroomId(),
	        								  chatroomUserDTO.getLastConnectionTime(),
	        								  chatroomUserDTO.getUserId());
	        	chatroomUserDTO.setLastConnectionTime(new Date());
	        	chatroomUserRepository.updateChatUserInfo(chatroomUserDTO.getConnectionStatus(),
	        			chatroomUserDTO.getLastConnectionTime(),
	        			chatroomUserDTO.getChatroomUserId());
	            break;	
	        case LEAVE :
	            // 접속 상태 OF로 세팅
	        	chatroomUserDTO.setConnectionStatus("OF");
	        	// 채팅방 마지막 접속 시간을 현재 시간으로 변경
	        	chatroomUserDTO.setLastConnectionTime(new Date());
	        	chatroomUserRepository.updateChatUserInfo(chatroomUserDTO.getConnectionStatus(),
	        			chatroomUserDTO.getLastConnectionTime(),
	        			chatroomUserDTO.getChatroomUserId());
	            break;
			case CHAT :
				// 현재 채팅방에 온라인 상태인 유저의 수를 구해서 메시지의 unreadCount를 세팅
				setUnreadCount(messageDTO, chatroomUserDTO);
	        	// 메시지를 DB에 저장
	        	messageRepository.save(MessageEntity.toEntity(messageDTO));
	        	// 채팅방의 마지막 메시지와 시간을 업데이트
	        	updateChatroomInfo(messageDTO, chatroomUserDTO);
	        	// 받는 사람들에게 메시지가 왔다고 브로드캐스팅 (채팅방 목록 실시간 갱신용)
	        	broadcastToChatUser(chatroomUserDTO);
	        	break;
	        default :
	            break;
		}
		
		// 메시지 브로드캐스팅 로직 호출
		broadcastToChatroom(messageDTO);
	}
	
	// 채팅방의 마지막 메시지와 시간을 업데이트
	private void updateChatroomInfo(MessageDTO messageDTO, ChatroomUserDTO chatroomUserDTO) {
		chatroomRepository.findById(chatroomUserDTO.getChatroomId())
				   .ifPresent(chatroom -> {
					   ChatroomDTO dto = ChatroomDTO.toDto(chatroom);
					   chatroomRepository.updateLastMsgAndDate(dto.getChatroomId(), messageDTO.getContent(), messageDTO.getSendDate());
				   });
	}
	
	// 현재 채팅방에 온라인 상태인 유저의 수를 구해서 메시지의 unreadCount를 세팅
	private void setUnreadCount(MessageDTO messageDTO, ChatroomUserDTO chatroomUserDTO) {
		int unreadCount = Math.max(messageDTO.getUnreadCount() - chatroomUserRepository.countOtherUsersOnline(chatroomUserDTO.getChatroomId(), chatroomUserDTO.getUserId()), 0);
		messageDTO.setUnreadCount(unreadCount);
	}
	
	// 받는 사람들에게 메시지가 왔다고 브로드캐스팅 (채팅방 목록 실시간 갱신용)
	private void broadcastToChatUser(ChatroomUserDTO chatroomUserDTO) {
		try {
			List<Long> receiverIds = chatroomUserRepository.getReceiverId(chatroomUserDTO.getChatroomId(), chatroomUserDTO.getUserId());
			for (Long receiverId : receiverIds) {
				Map<String, Object> data = new HashMap<>();
				data.put("chatroomId", chatroomUserDTO.getChatroomId());
				data.put("unreadCount", messageRepository.countUnreadMessages(chatroomUserDTO.getChatroomId(), receiverId));
				chatroomUserRepository.getChatroomUser(chatroomUserDTO.getChatroomId(), receiverId)
				.ifPresent(chatroomUserEntity -> data.put("chatroomUserId", chatroomUserEntity.getChatroomUserId()));
				
				messagingTemplate.convertAndSend("/sub/chatroom-list/" + receiverId, data);
			}
		} catch(MessagingException e) {
			// 메시지 전송 실패 시 상세 로그 출력
	        log.error("Failed to send message to chatroom {}: {}", 
	        		chatroomUserDTO.getChatroomId(), e.getMessage(), e);
	        // 런타임 예외
	        throw new IllegalStateException("유저 메시지 전송 중 오류가 발생했습니다.", e);
		} catch (Exception e) {
	        log.error("Unexpected error during message broadcast: {}", e.getMessage(), e);
	        throw new RuntimeException("예상치 못한 오류가 발생했습니다.", e);
	    }
		return;
	}
	
	private void broadcastToChatroom(MessageDTO messageDTO) {
		try {
			messagingTemplate.convertAndSend("/sub/messages/" + messageDTO.getChatroomId(), messageDTO);
		} catch (MessagingException e) {
	        // 메시지 전송 실패 시 상세 로그 출력
	        log.error("Failed to send message to chatroom {}: {}", 
	                  messageDTO.getChatroomId(), e.getMessage(), e);
	        // 런타임 예외
	        throw new IllegalStateException("메시지 전송 중 오류가 발생했습니다.", e);

	    } catch (Exception e) {
	        log.error("Unexpected error during message broadcast: {}", e.getMessage(), e);
	        throw new RuntimeException("예상치 못한 오류가 발생했습니다.", e);
	    }
		return;
	}
	
	// 상대방의 채팅방 접속 상태 확인 후 변경
	public void checkAndUpdateReceiverConnectionStatus(MessageUserDTO messageUserDTO, Date now) {
		ChatroomUserDTO chatroomUserDTO = messageUserDTO.getChatroomUserDTO();
		// 채팅 메시지를 보낸 사용자ID
		long userId = messageUserDTO.getMessageDTO().getUserId();
		long chatroomId = chatroomUserDTO.getChatroomId();
		// 상대방이 채팅방을 삭제한 상태인지 확인
		boolean ifDeleted = checkReceiverConnectionStatus(chatroomId, userId);
		if (ifDeleted) {
			String newChatroomName = chatUserRepository.findById(userId).get().getName() + "님과의 채팅방";
			
			// 삭제했다면 채팅방 접속상태 'OF'로 변경
			updateReceiverConnectionStatus(chatroomId, userId, newChatroomName, now);
		}
	}
	
	// 상대방의 채팅방 connectionStatus가 'DL'인지 확인
	@Transactional(readOnly = true)
	private boolean checkReceiverConnectionStatus(long chatroomId, long userId) {
		return chatroomUserRepository.checkIfChatroomDeleted(chatroomId, userId) > 0;
	}
	
	// 상대방의 채팅방 접속 상태를 'DL'에서 'OF'로 변경
	@Transactional
	private void updateReceiverConnectionStatus(long chatroomId, long userId, String newChatroomName, Date now) {
		chatroomUserRepository.updateReceiverConnectionStatus(chatroomId, userId, newChatroomName, now);
	}

}
