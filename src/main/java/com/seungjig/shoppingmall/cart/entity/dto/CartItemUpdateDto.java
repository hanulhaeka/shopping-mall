package com.seungjig.shoppingmall.cart.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemUpdateDto {

    private Long cartItemId;
    private Integer quantity;
}
