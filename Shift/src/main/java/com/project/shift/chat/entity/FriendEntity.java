package com.project.shift.chat.entity;

import com.project.shift.chat.dto.request.FriendDTO;

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
import lombok.Setter;

@Entity
@Table(name = "FRIENDS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(name = "USER_ID")
    private long userId;

    @Setter
    @Column(name = "FRIEND_ID")
    private long friendId;

    // DTO → Entity 변환
    public static FriendEntity toEntity(FriendDTO dto) {
        return FriendEntity.builder()
                .friendshipId(dto.getFriendshipId())
                .userId(dto.getUserId())
                .friendId(dto.getFriendId())
                .build();
    }
}
