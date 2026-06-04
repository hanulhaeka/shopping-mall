package com.seungjig.shoppingmall.product.controller;

import com.seungjig.shoppingmall.product.entity.dto.ProductCreateDto;
import com.seungjig.shoppingmall.product.exception.NotCorrectFileTypeException;
import com.seungjig.shoppingmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

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
            bindingResult.rejectValue("imageHead", "NotCorrectFileType", e.getMessage());
            return "product/create";
        }

        return "redirect:/";
    }

}
