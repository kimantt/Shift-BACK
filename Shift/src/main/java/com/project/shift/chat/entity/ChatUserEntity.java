package com.project.shift.chat.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.SQLRestriction;

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
@Table(name="USERS")
@SQLRestriction("DELETED_AT IS NULL")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserEntity {
	
    @Id
    @GeneratedValue(
	    strategy = GenerationType.SEQUENCE,
	    generator = "SEQ_USERS"
	)
	@SequenceGenerator(
	    name = "SEQ_USERS",
	    sequenceName = "SEQ_USERS",
	    allocationSize = 1
	)
    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "LOGIN_ID", unique = true)
    private String loginId;

    @Column(name = "PASSWORD", length = 100)
    private String password;

    @Column(name = "NAME", length = 20)
    private String name;

    @Column(name = "PHONE", length = 20, unique = true)
    private String phone;

    @Column(name = "ADDRESS", length = 200)
    private String address;

    @Column(name = "POINTS")
    private int points; // DEFAULT 0

    @Column(name = "ADMIN_FLAG", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String adminFlag;// DEFAULT 'N', 'Y' 또는 'N'
    
    @Column(name = "DELETED_AT")
    private Timestamp deletedAt;
    
}
