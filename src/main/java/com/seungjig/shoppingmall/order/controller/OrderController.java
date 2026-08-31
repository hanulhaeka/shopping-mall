package com.seungjig.shoppingmall.order.controller;

import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import com.seungjig.shoppingmall.order.entity.dto.OrderCreateDto;
import com.seungjig.shoppingmall.order.exception.IncorrectPriceException;
import com.seungjig.shoppingmall.order.exception.OutOfStockException;
import com.seungjig.shoppingmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 요청 처리
    @PostMapping("/create")
    public String createOrder(@Validated @ModelAttribute("orderCreateDto")OrderCreateDto orderCreateDto, BindingResult bindingResult,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            return "orders/create";
        }

        try {
            orderService.createOrder(orderCreateDto, userDetails.getUser(), orderCreateDto.getIsCartOrder());
        } catch (IncorrectPriceException e) {
            bindingResult.reject("incorrectPrice", "금액에 문제가 있습니다.");
            return "orders/create";
        } catch (OutOfStockException e) {
            bindingResult.reject("outOfStock", "재고가 부족합니다.");
            return "orders/create";
        }

        return "redirect:/orders/list";
    }
}
