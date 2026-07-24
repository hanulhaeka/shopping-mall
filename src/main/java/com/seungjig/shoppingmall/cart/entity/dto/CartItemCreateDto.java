package com.seungjig.shoppingmall.cart.entity.dto;

import lombok.Getter;

@Getter
public class CartItemCreateDto {

    private Long productId;

    private Integer itemQuantity;
}
