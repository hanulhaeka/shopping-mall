package com.seungjig.shoppingmall.order.controller;

import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import com.seungjig.shoppingmall.order.entity.dto.OrderCreateDto;
import com.seungjig.shoppingmall.order.entity.dto.OrderItemCreateDto;
import com.seungjig.shoppingmall.order.exception.IncorrectPriceException;
import com.seungjig.shoppingmall.order.exception.OutOfStockException;
import com.seungjig.shoppingmall.order.service.OrderService;
import com.seungjig.shoppingmall.product.service.ProductService;
import com.seungjig.shoppingmall.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 상품 상세에서 주문 페이지 반환
    @GetMapping("/create/direct")
    public String createDirectOrder(@RequestParam Long productId, @RequestParam Integer quantity,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model, RedirectAttributes redirectAttributes){

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "올바른 수량을 입력해주세요");
            return "redirect:/products/" + productId;
        }

        try {
            OrderCreateDto orderCreateDto = orderService.prepareDirectOrder(productId, quantity, userDetails.getUser());
            model.addAttribute("orderCreateDto", orderCreateDto);
            return "order/create";
        } catch (OutOfStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + productId;
        }

    }

    // 주문 요청 처리
    @PostMapping("/create")
    public String createOrder(@Validated @ModelAttribute("orderCreateDto")OrderCreateDto orderCreateDto, BindingResult bindingResult,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            return "order/create";
        }

        try {
            orderCreateDto.setIsCartOrder(false);
            orderService.createOrder(orderCreateDto, userDetails.getUser(), orderCreateDto.getIsCartOrder());
        } catch (IncorrectPriceException e) {
            bindingResult.reject("incorrectPrice", "금액에 문제가 있습니다.");
            return "order/create";
        } catch (OutOfStockException e) {
            bindingResult.reject("outOfStock", "재고가 부족합니다.");
            return "order/create";
        }

        return "redirect:/";
    }
}
