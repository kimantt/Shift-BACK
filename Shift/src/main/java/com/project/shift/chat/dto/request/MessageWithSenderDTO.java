package com.project.shift.chat.dto.request;

import com.project.shift.chat.dto.response.ChatroomUserDTO;
import com.project.shift.chat.dto.response.MessageDTO;

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
public class MessageWithSenderDTO {

    private MessageDTO message;
    private ChatroomUserDTO sender;
    private long receiverId;
    private String senderName;
}
