package com.project.shift.chat.dto.response;

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
public class ChatUserSearchResultDTO {

	private boolean ifFriend;
    private long userId;
    private String loginId;
    private String name;
    private String phone;

}
