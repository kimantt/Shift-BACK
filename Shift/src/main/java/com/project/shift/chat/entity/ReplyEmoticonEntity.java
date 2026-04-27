package com.project.shift.chat.entity;

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
@Table(name="REPLY_EMOTICONS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyEmoticonEntity {

	@Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_REPLY_EMOTICONS"
    )
    @SequenceGenerator(
        name = "SEQ_REPLY_EMOTICONS",
        sequenceName = "SEQ_REPLY_EMOTICONS",
        allocationSize = 1
    )
    @Column(name = "REPLY_EMOTICON_ID", nullable = false)
    private long replyEmoticonId;

    @Column(name = "MESSAGE_ID", nullable = false)
    private long messageId;

    @Column(name = "USER_ID")
    private long userId;
    
    @Column(name = "TYPE", nullable = false, length = 3)
    private String type;
}