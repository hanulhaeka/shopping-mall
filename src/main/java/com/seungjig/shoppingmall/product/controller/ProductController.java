package com.seungjig.shoppingmall.product.controller;

import com.seungjig.shoppingmall.global.util.FileService;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.entity.dto.AdminProductListDto;
import com.seungjig.shoppingmall.product.entity.dto.ProductCreateDto;
import com.seungjig.shoppingmall.product.entity.dto.ProductListDto;
import com.seungjig.shoppingmall.product.entity.dto.ProductUpdateDto;
import com.seungjig.shoppingmall.product.exception.NotCorrectFileTypeException;
import com.seungjig.shoppingmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileService fileService;

    @ModelAttribute("productStatuses")
    public ProductStatus[] productStatuses() {
        return Arrays.stream(ProductStatus.values())
                .filter(status -> status != ProductStatus.DELETE)
                .toArray(ProductStatus[]::new);
    }

    // 상품 등록 폼 전송
    @GetMapping("/create")
    public String createProduct(@ModelAttribute("productCreateDto") ProductCreateDto productCreateDto) {
        return "product/create";
    }

    // 상품 등록 요청 처리
    @PostMapping("/create")
    public String createProduct(@Validated @ModelAttribute("productCreateDto") ProductCreateDto productCreateDto, BindingResult bindingResult) throws IOException {

        if (productCreateDto.getImageHead() == null || productCreateDto.getImageHead().isEmpty()) {
            bindingResult.rejectValue("imageHead", "NotHaveNeededFile", "필수 제출 파일이 없습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "product/create";
        }

        try {
            productService.createProduct(productCreateDto);
        } catch (NotCorrectFileTypeException e) {
            bindingResult.rejectValue("imageHead", "invalidType", e.getMessage());
            return "product/create";
        }

        return "redirect:/";
    }

    // 관리자 상품 목록 조회
    @GetMapping("/admin/list")
    public String getAdminProductList(@PageableDefault(page = 0, size = 10) Pageable pageable, Model model) {
        Page<AdminProductListDto> page = productService.getAdminProductList(pageable);
        model.addAttribute("page", page);

        return "product/admin-product-list";
    }

    // 상품 수정 폼 전송
    @GetMapping("/update/{productId}")
    public String updateProductForm(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("productUpdateDto", productService.getProductUpdateDto(productId));
        return "product/update";
    }

    // 상품 수정 요청 처리
    @PostMapping("/update")
    public String updateProduct(@Validated @ModelAttribute("productUpdateDto") ProductUpdateDto productUpdateDto, BindingResult bindingResult) throws IOException {

        if (productUpdateDto.getImageHead() != null && !productUpdateDto.getImageHead().isEmpty()
                && !fileService.isImageFile(productUpdateDto.getImageHead())) {
            bindingResult.rejectValue("imageHead", "invalidType", "이미지 파일이 아닙니다.");
        }

        validateImageFile(bindingResult, productUpdateDto.getImage1(), productUpdateDto.getIsImage1Deleted(), "image1");
        validateImageFile(bindingResult, productUpdateDto.getImage2(), productUpdateDto.getIsImage2Deleted(), "image2");
        validateImageFile(bindingResult, productUpdateDto.getImage3(), productUpdateDto.getIsImage3Deleted(), "image3");

        if (bindingResult.hasErrors()) {
            return "product/update";
        }

        productService.updateProduct(productUpdateDto);

        return "redirect:/products/admin/list";

    }

    // 상품 목록 (일반 유저용) 조회
    @GetMapping("/list")
    public String getProductList(@PageableDefault(page = 0, size = 6) Pageable pageable, Model model) {
        Page<ProductListDto> page = productService.getProductList(pageable);
        model.addAttribute("page", page);

        return "product/product-list";
    }

    // 상품 단건 상세 조회
    @GetMapping("/{productId}")
    public String getProductDetail(@PathVariable("productId") Long productId, Model model) {
        model.addAttribute("productDetailDto", productService.getProductDetailDto(productId));
        return "product/product-detail";
    }

    private void validateImageFile(BindingResult bindingResult, MultipartFile file, Boolean isDeleted, String fieldName) {

        boolean safeIsDeleted = Boolean.TRUE.equals(isDeleted);

        if (!safeIsDeleted && file != null && !file.isEmpty()) {
            if (!fileService.isImageFile(file)) {
                bindingResult.rejectValue(fieldName, "invalidType", "이미지 파일이 아닙니다.");
            }
        }
    }

}
