package com.project.shift.chat.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.project.shift.chat.dto.response.ChatroomDTO;

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
@Table(name="CHATROOMS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // DTO -> Entity 변환
    public static ChatroomEntity toEntity(ChatroomDTO dto) {
        return ChatroomEntity.builder()
                .lastMsgContent(dto.getLastMsgContent())
                .lastMsgDate(dto.getLastMsgDate())
                .build();
    }

}