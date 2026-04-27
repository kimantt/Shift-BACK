package com.project.shift.chat.dto.response;

import java.util.Date;

import com.project.shift.chat.entity.MessageEntity;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

	public enum MessageType {
        CHAT, JOIN, LEAVE
    }
	@Transient //DB와 매핑하지 않는 필드
	@Setter
	private MessageType type;
	
    private long messageId;
    @Setter
    private long chatroomId;
    @Setter
    private long userId;
    @Setter
    private Date sendDate;
    @Setter
    private String content;
    @Setter
    private String isGift;
    @Setter
    private int unreadCount;

    // Entity -> DTO 변환
    public static MessageDTO toDto(MessageEntity entity) {
        return MessageDTO.builder()
                .messageId(entity.getMessageId())
                .chatroomId(entity.getChatroomId())
                .userId(entity.getUserId())
                .sendDate(entity.getSendDate())
                .content(entity.getContent())
                .isGift(entity.getIsGift())
                .unreadCount(entity.getUnreadCount())
                .build();
    }

}
