package com.project.shift.chat.dto;

import com.project.shift.chat.entity.ChatUserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserDTO {

    private long userId;
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String address;
    private int points;
    private String adminFlag; // DEFAULT 'N', 'Y' 또는 'N'

    // Entity -> DTO 변환
    public static ChatUserDTO toDto(ChatUserEntity entity) {
        return ChatUserDTO.builder()
                .userId(entity.getUserId())
                .loginId(entity.getLoginId())
                .password(entity.getPassword())
                .name(entity.getName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .points(entity.getPoints())
                .adminFlag(entity.getAdminFlag())
                .build(); // 생성자 호출
    }

    // 채팅방 친구 목록에 띄운 사용자 정보
	public static ChatUserDTO toFriendUserDTO(ChatUserEntity entity) {
		return ChatUserDTO.builder()
                .userId(entity.getUserId())
                .loginId(entity.getLoginId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .build(); // 생성자 호출
	}

}
