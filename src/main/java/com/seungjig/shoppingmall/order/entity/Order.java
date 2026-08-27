package com.seungjig.shoppingmall.order.entity;

import com.seungjig.shoppingmall.global.entity.BaseEntity;
import com.seungjig.shoppingmall.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name="orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 255)
    private String addressDetail;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String ordererName;

    @Column(length = 30)
    private String ordererPhoneNumber;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(length = 30)
    private String recipientPhoneNumber;

}
