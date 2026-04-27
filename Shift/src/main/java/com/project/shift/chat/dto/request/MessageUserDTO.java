package com.project.shift.chat.dto.request;

import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.dto.response.MessageDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageUserDTO {

	private MessageDTO messageDTO;
	private ChatroomUserDTO chatroomUserDTO;
}
