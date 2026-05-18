package com.seungjig.shoppingmall.user.entity;

import com.seungjig.shoppingmall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    public User(String loginId, String hashedPassword, String name, String phoneNumber, Role role, UserStatus status) {
        this.loginId = loginId;
        this.hashedPassword = hashedPassword;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;



}
