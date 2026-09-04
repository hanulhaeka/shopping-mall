package com.seungjig.shoppingmall.order.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemCreateDto {
    @NotNull(message = "상품 아이디는 필수입니다.")
    private Long productId;
    @NotNull(message = "주문 수량 정보는 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다.")
    private Integer quantity;
    @NotNull(message = "상품 가격 정보는 필수입니다.")
    @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
    private Integer unitPrice;
    private String imageHeadUrl;
}
