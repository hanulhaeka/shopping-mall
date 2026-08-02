package com.seungjig.shoppingmall.cart.entity;

import com.seungjig.shoppingmall.global.entity.BaseEntity;
import com.seungjig.shoppingmall.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    public CartItem(Cart cart, Product product, Integer quantity, Integer unitPrice) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPrice;


    public void changeQuantity(Integer itemQuantity) {
        this.quantity += itemQuantity;
    }
}
