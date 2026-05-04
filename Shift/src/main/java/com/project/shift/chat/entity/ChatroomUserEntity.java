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
@Table(name="CHATROOM_USERS")
@Getter
@NoArgsConstructor
public class ChatroomUserEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_CHATROOM_USERS"
    )
    @SequenceGenerator(
        name = "SEQ_CHATROOM_USERS",
        sequenceName = "SEQ_CHATROOM_USERS",
        allocationSize = 1
    )
    @Column(name = "CHATROOM_USERS_ID", nullable = false)
    private long chatroomUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHATROOM_ID", nullable = false)
    private ChatroomEntity chatroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @Column(name = "CHATROOM_NAME", length = 30)
    private String chatroomName;

    @Column(name = "LAST_CONNECTION_TIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastConnectionTime;
    
    @Column(name = "CREATED_TIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @Column(name = "CONNECTION_STATUS", nullable = false, length = 2)
    private String connectionStatus;

    @Column(name = "IS_DARK_MODE", nullable = false, length = 1, columnDefinition = "CHAR(1) default 'N'")
    private String isDarkMode;
    
    @Builder
    public ChatroomUserEntity(long chatroomUserId, ChatroomEntity chatroom, UserEntity user, String chatroomName,
            Date lastConnectionTime, Date createdTime, String connectionStatus, String isDarkMode) {
        this.chatroomUserId = chatroomUserId;
        this.chatroom = chatroom;
        this.user = user;
        this.chatroomName = chatroomName;
        this.lastConnectionTime = lastConnectionTime;
        this.createdTime = createdTime;
        this.connectionStatus = connectionStatus;
        this.isDarkMode = isDarkMode;
    }
    
    public static ChatroomUserEntity of(
            ChatroomEntity chatroom,
            UserEntity user,
            String chatroomName,
            Date lastConnectionTime,
            Date createdTime,
            String connectionStatus,
            String isDarkMode
    ) {
        return ChatroomUserEntity.builder()
                .chatroom(chatroom)
                .user(user)
                .chatroomName(chatroomName)
                .lastConnectionTime(lastConnectionTime)
                .createdTime(createdTime)
                .connectionStatus(connectionStatus)
                .isDarkMode(isDarkMode)
                .build();
    }
}