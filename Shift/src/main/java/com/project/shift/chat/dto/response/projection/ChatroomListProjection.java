package com.project.shift.chat.dto.response.projection;

public interface ChatroomListProjection {
	Long getChatroomUserId();
    Long getChatroomId();
    String getChatroomName();
    String getLastMsgContent();

    Long getLastMsgSender();
    java.sql.Timestamp getLastMsgDate();
    java.sql.Timestamp getLastConnectionTime();
    java.sql.Timestamp getCreatedTime();

    String getConnectionStatus();
    String getIsDarkMode();
    Long getReceiverId();
    String getReceiverName();
}
