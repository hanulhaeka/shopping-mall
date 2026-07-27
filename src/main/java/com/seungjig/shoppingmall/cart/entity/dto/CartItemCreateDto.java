package com.seungjig.shoppingmall.cart.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CartItemCreateDto {

    @NotNull(message = "상품 아이디가 필요합니다.")
    private Long productId;

    @NotNull(message = "수량이 필요합니다.")
    @Positive(message = "1이상의 수를 선택해주세요")
    private Integer itemQuantity;
}
