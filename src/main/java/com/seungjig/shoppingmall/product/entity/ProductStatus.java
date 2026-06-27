package com.seungjig.shoppingmall.product.entity;

public enum ProductStatus {
    ACTIVE("판매중"),
    INACTIVE("판매종료");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
