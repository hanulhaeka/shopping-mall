package com.seungjig.shoppingmall.order.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderCreateDto {
    @NotNull(message = "주문 총합 가격은 필수입니다.")
    @Min(value = 0, message = "주문 총합 가격은 0원 이상이어야 합니다.")
    private Integer totalPrice;
    @NotBlank(message = "주소를 입력해야 합니다.")
    private String address;
    @NotBlank(message = "상세 주소를 입력해야 합니다.")
    private String addressDetail;
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    @NotBlank(message = "우편번호를 입력해야 합니다.")
    private String postalCode;
    @NotBlank(message = "주문인 이름을 입력해야 합니다.")
    private String ordererName;
    @Pattern(regexp = "^$|^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String ordererPhoneNumber;
    @NotBlank(message = "수령인 이름을 입력해야 합니다.")
    private String recipientName;
    @Pattern(regexp = "^$|^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String recipientPhoneNumber;
    @Valid
    @NotEmpty(message = "주문 상품 목록은 최소 1개 이상이어야 합니다.")
    private List<OrderItemCreateDto> orderList = new ArrayList<>();

    Boolean isCartOrder;
}
