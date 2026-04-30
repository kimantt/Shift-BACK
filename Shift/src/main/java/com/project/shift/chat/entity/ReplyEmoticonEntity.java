package com.project.shift.chat.entity;

import com.project.shift.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="REPLY_EMOTICONS")
@Getter
@NoArgsConstructor
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

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_ID", nullable = false)
    private MessageEntity message;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;
    
    @Column(name = "TYPE", nullable = false, length = 3)
    private String type;
    
    @Builder
    public ReplyEmoticonEntity(long replyEmoticonId, MessageEntity message, UserEntity user, String type) {
        this.replyEmoticonId = replyEmoticonId;
        this.message = message;
        this.user = user;
        this.type = type;
    }
}