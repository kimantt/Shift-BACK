package com.project.shift.chat.entity;

import com.project.shift.chat.dto.request.FriendDTO;
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
import lombok.Setter;

@Entity
@Table(name = "FRIENDS")
@Getter
@NoArgsConstructor
public class FriendEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "SEQ_FRIENDS"
    )
    @SequenceGenerator(
        name = "SEQ_FRIENDS",
        sequenceName = "SEQ_FRIENDS",
        allocationSize = 1
    )
    @Column(name = "FRIENDSHIP_ID")
    private long friendshipId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FRIEND_ID")
    private UserEntity friend;

    @Builder
    public FriendEntity(long friendshipId, UserEntity user, UserEntity friend) {
        this.friendshipId = friendshipId;
        this.user = user;
        this.friend = friend;
    }
    
    public static FriendEntity from(FriendDTO dto, UserEntity user, UserEntity friend) {
        return FriendEntity.builder()
                .friendshipId(dto.getFriendshipId())
                .user(user)
                .friend(friend)
                .build();
    }
}
