package com.seungjig.shoppingmall.order.entity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemDetailDto {
    private String productImageUrl;
    private String productName;
    private Integer quantity;
    private Integer unitPrice;
}
