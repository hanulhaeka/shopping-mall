package com.seungjig.shoppingmall.cart.entity.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CartListDto {

    Long cartItemId;
    Long productId;
    String productName;
    String productImageUrl;
    Integer unitPrice;
    Integer quantity;
    Integer totalPrice;

}
