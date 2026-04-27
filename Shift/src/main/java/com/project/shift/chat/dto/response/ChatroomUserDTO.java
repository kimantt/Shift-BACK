package com.project.shift.chat.dto.response;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.project.shift.chat.entity.ChatroomUserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatroomUserDTO {

	// 0이 기본값으로 들어가는 방지하기 위해서 long → Long으로 타입 변경
	private Long chatroomUserId;
    private long chatroomId;
    private long userId;
    private String chatroomName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastConnectionTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;
    private String connectionStatus;
    private String isDarkMode;
    
    // Entity -> DTO 변환
    public static ChatroomUserDTO toDto(ChatroomUserEntity entity) {
        return ChatroomUserDTO.builder()
        		.chatroomUserId(entity.getChatroomUserId())
                .chatroomId(entity.getChatroomId())
                .userId(entity.getUserId())
                .chatroomName(entity.getChatroomName())
                .lastConnectionTime(entity.getLastConnectionTime())
                .createdTime(entity.getCreatedTime())
                .connectionStatus(entity.getConnectionStatus())
                .isDarkMode(entity.getIsDarkMode())
                .build();
    }    
    
}
