package com.seungjig.shoppingmall.cart.controller;

import com.seungjig.shoppingmall.cart.entity.dto.CartItemCreateDto;
import com.seungjig.shoppingmall.cart.entity.dto.CartItemUpdateDto;
import com.seungjig.shoppingmall.cart.entity.dto.CartListDto;
import com.seungjig.shoppingmall.cart.service.CartService;
import com.seungjig.shoppingmall.global.exception.ErrorResponse;
import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // 장바구니 목록 조회
    @GetMapping
    public String getCartList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUserId();
        List<CartListDto> cartList = cartService.getCartList(userId);
        model.addAttribute("cartList", cartList);

        return "cart/cart-list";
    }

    // 장바구니 상품 수량 변경
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItemQuantity(@RequestBody CartItemUpdateDto cartItemUpdateDto,@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.changeCartItemQuantity(cartItemUpdateDto.getCartItemId(), userDetails.getUserId(), cartItemUpdateDto.getQuantity());
        return ResponseEntity.ok(Map.of(
                "message", "수량이 성공적으로 변경되었습니다.",
                "quantity", cartItemUpdateDto.getQuantity()
        ));
    }

    // 장바구니 상품 수량 조회
    @GetMapping("/items/{cartItemId}/quantity")
    @ResponseBody
    public ResponseEntity<?> getCartItemQuantity(@PathVariable Long cartItemId) {
        Integer cartItemQuantity = cartService.getCartItemQuantity(cartItemId);
        return ResponseEntity.ok(Map.of(
                "quantity", cartItemQuantity
        ));
    }
}
