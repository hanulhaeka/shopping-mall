package com.seungjig.shoppingmall.order.entity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderDetailDto {
    private String ordererName;
    private String ordererPhoneNumber;
    private Integer totalPrice;
    private LocalDateTime orderDateTime;
    private String address;
    private String addressDetail;
    private String postalCode;
    private String recipientName;
    private String recipientPhoneNumber;
    private List<OrderItemDetailDto> orderItemDtos;
}
