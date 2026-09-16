package com.seungjig.shoppingmall.order.entity.dto;

import com.seungjig.shoppingmall.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderListDto {
    private Long orderId;
    private String mainProductName;
    private Integer productCount;
    private Integer totalPrice;
    private String mainProductImageUrl;
    private LocalDate orderTime;
    private OrderStatus orderStatus;
}
