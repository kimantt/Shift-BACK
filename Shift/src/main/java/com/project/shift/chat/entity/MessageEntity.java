package com.project.shift.chat.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.project.shift.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="MESSAGES")
@Getter
@NoArgsConstructor
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

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHATROOM_ID", nullable = false)
    private ChatroomEntity chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @Column(name = "SEND_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendDate;

    @Column(name = "CONTENT", nullable = false, length = 300)
    private String content;
    
    @Column(name = "IS_GIFT", nullable = false, length = 1, columnDefinition = "CHAR(1) default 'N'")
    private String isGift;
    
    @Column(name = "UNREAD_COUNT", nullable = false)
    private int unreadCount;

    @Builder
    public MessageEntity(long messageId, ChatroomEntity chatroom, UserEntity user, Date sendDate, String content,
            String isGift, int unreadCount) {
        this.messageId = messageId;
        this.chatroom = chatroom;
        this.user = user;
        this.sendDate = sendDate;
        this.content = content;
        this.isGift = isGift;
        this.unreadCount = unreadCount;
    }
    
    public static MessageEntity of(
            long messageId,
            ChatroomEntity chatroom,
            UserEntity user,
            Date sendDate,
            String content,
            String isGift,
            int unreadCount
    ) {
        return MessageEntity.builder()
                .messageId(messageId)
                .chatroom(chatroom)
                .user(user)
                .sendDate(sendDate)
                .content(content)
                .isGift(isGift)
                .unreadCount(unreadCount)
                .build();
    }
}