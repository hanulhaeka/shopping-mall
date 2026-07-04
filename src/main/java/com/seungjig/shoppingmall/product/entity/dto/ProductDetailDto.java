package com.seungjig.shoppingmall.product.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDetailDto {
    private Long id;
    private String name;
    private Integer price;
    private String imageHeadUrl;
    private String image1Url;
    private String image2Url;
    private String image3Url;
}
