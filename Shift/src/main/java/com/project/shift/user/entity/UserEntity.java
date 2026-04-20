package com.project.shift.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import com.project.shift.chat.entity.ChatroomUserEntity;
import com.project.shift.chat.entity.FriendEntity;
import com.project.shift.chat.entity.MessageEntity;
import com.project.shift.chat.entity.ReplyEmoticonEntity;
import com.project.shift.product.entity.PointTransaction;
import com.project.shift.product.entity.Review;
import com.project.shift.shop.entity.Cart;
import com.project.shift.shop.entity.Order;
import com.project.shift.auth.entity.RefreshTokenEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@SequenceGenerator(
        name = "users_seq_generator",
        sequenceName = "seq_users",
        allocationSize = 1
)
@SQLRestriction("DELETED_AT IS NULL") //DELETED_AT이 NULL인 값만 조회하도록 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_generator")
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Setter
    @Column(nullable = false)
    private Integer points;

    @Column(name = "admin_flag", nullable = false, length = 1)
    private String adminFlag;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Builder
    public UserEntity(String loginId,
                String password,
                String name,
                String phone,
                String address,
                Integer points,
                String adminFlag,
                LocalDateTime deletedAt) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.points = (points == null) ? 0 : points;
        this.adminFlag = (adminFlag == null) ? "N" : adminFlag;
        this.deletedAt = deletedAt;
    }

    //수정 가능 필드만 메서드로 제공
    public void updateInfo(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
    
    // 회원 탈퇴 처리 (로그인 ID 변경, 비밀번호 폐기, 개인정보 초기화)
    public void withdraw(String deletedLoginId, String discardedPassword, LocalDateTime deletedAt) {
        this.loginId = deletedLoginId;
        this.password = discardedPassword;
        this.name = "탈퇴한 사용자";
        this.phone = null;
        this.address = null;
        this.points = 0;
        this.deletedAt = deletedAt;
    }
    
    // --------------------
    // 연관관계
    // --------------------
    
//    // friends.user_id
//    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
//    private List<FriendEntity> outgoingFriendships = new ArrayList<>();
//
//    // friends.friend_id
//    @OneToMany(mappedBy = "friendId", fetch = FetchType.LAZY)
//    private List<FriendEntity> incomingFriendships = new ArrayList<>();
//
//    // cart_items.user_id
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Cart> cartItems = new ArrayList<>();
//
//    // orders.sender_id
//    @OneToMany(mappedBy = "senderId", fetch = FetchType.LAZY)
//    private List<Order> sentOrders = new ArrayList<>();
//
//    // orders.receiver_id
//    @OneToMany(mappedBy = "receiverId", fetch = FetchType.LAZY)
//    private List<Order> receivedOrders = new ArrayList<>();
//
//    // reviews.user_id
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Review> reviews = new ArrayList<>();
//
//    // chatroom_users.user_id
//    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
//    private List<ChatroomUserEntity> chatroomUsers = new ArrayList<>();
//
//    // messages.user_id
//    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
//    private List<MessageEntity> messages = new ArrayList<>();
//
//    // reply_emoticons.user_id
//    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
//    private List<ReplyEmoticonEntity> replyEmoticons = new ArrayList<>();
//
//    // point_transactions.user_id
//    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY)
//    private List<PointTransaction> pointTransactions = new ArrayList<>();
//
//    // refreshtokens.user_id
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
//    private RefreshTokenEntity refreshToken;
}