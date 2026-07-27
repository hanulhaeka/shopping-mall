package com.seungjig.shoppingmall.cart.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartControllerTest.class)
public class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("인증되지 않은 사용자가 장바구니 추가 요청 시 401 UnAuthorized 응답 반환")
    void createCartItem_unauthorized() throws Exception {
        mockMvc.perform(post("/carts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 1, \"itemQuantity\": 2}")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
