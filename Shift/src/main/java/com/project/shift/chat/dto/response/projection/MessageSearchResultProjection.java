package com.project.shift.chat.dto.response.projection;

public interface MessageSearchResultProjection {

	Long getChatroomUserId();
    Long getChatroomId();
    String getChatroomName();

    java.sql.Timestamp getLastConnectionTime();
    java.sql.Timestamp getCreatedTime();

    String getConnectionStatus();
    String getIsDarkMode();
    String getMessage();
    java.sql.Timestamp getSendDate();
    Long getReceiverId();
}
