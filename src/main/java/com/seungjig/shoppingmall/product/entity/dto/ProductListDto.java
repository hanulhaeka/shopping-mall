package com.seungjig.shoppingmall.product.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductListDto {
    private Long id;
    private String imageUrl;
    private String name;
    private Integer price;
}
