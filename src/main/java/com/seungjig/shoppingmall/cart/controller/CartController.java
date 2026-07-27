package com.seungjig.shoppingmall.cart.controller;

import com.seungjig.shoppingmall.cart.entity.dto.CartItemCreateDto;
import com.seungjig.shoppingmall.cart.service.CartService;
import com.seungjig.shoppingmall.global.exception.ErrorResponse;
import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 상품 추가
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createCartItem(@Validated @RequestBody CartItemCreateDto cartItemCreateDto, BindingResult bindingResult,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", errorMessage));
        }

        cartService.createCartItem(cartItemCreateDto, userDetails.getUserId());
        return ResponseEntity.ok("장바구니에 상품이 담겼습니다.");
    }
}
