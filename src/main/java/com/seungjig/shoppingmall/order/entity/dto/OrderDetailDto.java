package com.seungjig.shoppingmall.order.entity.dto;

import com.seungjig.shoppingmall.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderDetailDto {
    private Long orderId;
    private String ordererName;
    private String ordererPhoneNumber;
    private Integer totalPrice;
    private LocalDateTime orderDateTime;
    private String address;
    private String addressDetail;
    private String postalCode;
    private String recipientName;
    private String recipientPhoneNumber;
    private OrderStatus orderStatus;
    private List<OrderItemDetailDto> orderItemDtos;
}
