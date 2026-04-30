package com.project.shift.chat.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="CHATROOMS")
@Getter
@NoArgsConstructor
public class ChatroomEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_CHATROOMS"
    )
    @SequenceGenerator(
        name = "SEQ_CHATROOMS",
        sequenceName = "SEQ_CHATROOMS",
        allocationSize = 1
    )
    @Column(name = "CHATROOM_ID", nullable = false)
    private Long chatroomId;

    @Column(name = "LAST_MSG_CONTENT")
    private String lastMsgContent;

    @Column(name = "LAST_MSG_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastMsgDate;

    @OneToMany(mappedBy = "chatroom", fetch = FetchType.LAZY)
    private List<ChatroomUserEntity> chatroomUsers = new ArrayList<>();

    @OneToMany(mappedBy = "chatroom", fetch = FetchType.LAZY)
    private List<MessageEntity> messages = new ArrayList<>();

    @Builder
    public ChatroomEntity(Long chatroomId, String lastMsgContent, Date lastMsgDate) {
        this.chatroomId = chatroomId;
        this.lastMsgContent = lastMsgContent;
        this.lastMsgDate = lastMsgDate;
    }
}