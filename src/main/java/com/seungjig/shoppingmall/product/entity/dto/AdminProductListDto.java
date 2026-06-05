package com.seungjig.shoppingmall.product.entity.dto;

import com.seungjig.shoppingmall.product.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminProductListDto {

    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private ProductStatus status;
}
