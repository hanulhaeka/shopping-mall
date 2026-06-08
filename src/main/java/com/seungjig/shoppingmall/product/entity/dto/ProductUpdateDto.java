package com.seungjig.shoppingmall.product.entity.dto;

import com.seungjig.shoppingmall.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateDto {

    @NotNull
    private Long id;

    @NotBlank(message = "상품명을 입력해야 합니다.")
    private String name;
    @NotNull(message = "가격을 입력해야 합니다.")
    @PositiveOrZero(message = "올바른 가격을 입력해주세요.")
    private Integer price;
    @NotNull(message = "재고를 입력해야 합니다.")
    @PositiveOrZero(message = "올바른 재고 수량을 입력해주세요.")
    private Integer stock;
    @NotNull(message="상품 상태 필수 선택입니다.")
    private ProductStatus status;

    private String imageHeadUrl;
    private String image1Url;
    private String image2Url;
    private String image3Url;

    private MultipartFile imageHead;
    private MultipartFile image1;
    private MultipartFile image2;
    private MultipartFile image3;

    private Boolean isImage1Deleted;
    private Boolean isImage2Deleted;
    private Boolean isImage3Deleted;
}
