package com.seungjig.shoppingmall.product.entity;

import com.seungjig.shoppingmall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name="products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false, length = 500)
    private String imageHeadUrl;

    @Column(length = 500)
    private String image1Url;

    @Column(length = 500)
    private String image2Url;

    @Column(length = 500)
    private String image3Url;

}
