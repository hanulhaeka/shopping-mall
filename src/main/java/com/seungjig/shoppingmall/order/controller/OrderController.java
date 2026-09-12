package com.seungjig.shoppingmall.order.controller;

import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import com.seungjig.shoppingmall.order.entity.dto.OrderCreateDto;
import com.seungjig.shoppingmall.order.exception.IncorrectPriceException;
import com.seungjig.shoppingmall.order.exception.OutOfStockException;
import com.seungjig.shoppingmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

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

    // 장바구니에서 주문 페이지 이동
    @GetMapping("/create/cart")
    public String createCartOrder(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {

        try {
            OrderCreateDto orderCreateDto = orderService.prepareCartOrder(userDetails.getUser());
            model.addAttribute("orderCreateDto", orderCreateDto);
            return "order/create";
        } catch (OutOfStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/carts";
        }

    }

    // 주문 요청 처리
    @PostMapping("/create")
    public String createOrder(@Validated @ModelAttribute("orderCreateDto")OrderCreateDto orderCreateDto, BindingResult bindingResult,
                              @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors("totalPrice") ||
                    bindingResult.hasFieldErrors("ordererName") ||
                    bindingResult.hasFieldErrors("ordererPhoneNumber") ||
                    bindingResult.hasFieldErrors("orderList") ||
                    hasOrderItemBindingErrors(bindingResult)) {
                bindingResult.reject("order.invalid", "주문 정보를 다시 확인해주세요.");
            }

            model.addAttribute(
                    "globalErrors",
                    bindingResult.getGlobalErrors()
                            .stream()
                            .map(ObjectError::getDefaultMessage)
                            .toList()
            );

            return "order/create";
        }

        try {
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

    // 주문 목록 조회
    @GetMapping
    public String getOrderList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model,
                               @PageableDefault(size = 5, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("orderList", orderService.getOrderListDto(userDetails.getUser(), pageable));
        return "order/list";
    }


    private boolean hasOrderItemBindingErrors(BindingResult bindingResult) {
        Set<String> targetFields = Set.of(
                "quantity",
                "productId",
                "price",
                "unitPrice"
        );

        return bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .map(field -> field.replaceAll("orderList\\[\\d+\\]\\.", ""))
                .anyMatch(targetFields::contains);
    }
}
