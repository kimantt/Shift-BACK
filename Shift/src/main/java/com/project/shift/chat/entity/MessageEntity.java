package com.project.shift.chat.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.project.shift.chat.dto.response.MessageDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="MESSAGES")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {
    
	@Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_MESSAGES"
    )
    @SequenceGenerator(
        name = "SEQ_MESSAGES",
        sequenceName = "SEQ_MESSAGES",
        allocationSize = 1
    )
    @Column(name = "MESSAGE_ID", nullable = false)
    private long messageId;

    @Column(name = "CHATROOM_ID", nullable = false)
    private long chatroomId;
    
    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "SEND_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendDate;

    @Column(name = "CONTENT", nullable = false, length = 300)
    private String content;
    
    @Column(name = "IS_GIFT", nullable = false, length = 1, columnDefinition = "CHAR(1) default 'N'")
    private String isGift;
    
    @Column(name = "UNREAD_COUNT", nullable = false)
    private int unreadCount;

    // DTO -> Entity 변환
    public static MessageEntity toEntity(MessageDTO dto) {
        return MessageEntity.builder()
                .messageId(dto.getMessageId())
                .chatroomId(dto.getChatroomId())
                .userId(dto.getUserId())
                .sendDate(dto.getSendDate())
                .content(dto.getContent())
                .isGift(dto.getIsGift())
                .unreadCount(dto.getUnreadCount())
                .build();
    }

}