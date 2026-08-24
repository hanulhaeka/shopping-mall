package com.seungjig.shoppingmall.cart.service;

import com.seungjig.shoppingmall.cart.entity.Cart;
import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.cart.entity.dto.CartItemCreateDto;
import com.seungjig.shoppingmall.cart.repository.CartItemRepository;
import com.seungjig.shoppingmall.cart.repository.CartRepository;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("장바구니에 이미 담긴 상품을 추가하면 수량이 합산된다.")
    void createCartItem_alreadyExists_addQuantity() {
        //given
        User user = new User("loginId", "password", "name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        user.setId(1L);

        Cart cart = new Cart(user);
        cart.setId(1L);

        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setId(1L);

        CartItem cartItem = new CartItem(cart, product, 1, 10000);

        CartItemCreateDto cartItemCreateDto = new CartItemCreateDto();
        cartItemCreateDto.setProductId(product.getId());
        cartItemCreateDto.setItemQuantity(5);

        given(cartRepository.findByUserId(user.getId())).willReturn(Optional.of(cart));
        given(productRepository.findById(cartItemCreateDto.getProductId())).willReturn(Optional.of(product));
        given(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())).willReturn(Optional.of(cartItem));

        //when
        cartService.createCartItem(cartItemCreateDto, user.getId());

        //then
        assertThat(cartItem.getQuantity()).isEqualTo(6);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    void deleteCartItem_success() {
        //given
        User user = new User("loginId", "password", "name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        user.setId(1L);

        Cart cart = new Cart(user);
        cart.setId(1L);

        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setId(1L);

        CartItem cartItem = new CartItem(cart, product, 1, 10000);
        cartItem.setId(1L);

        given(cartItemRepository.findByIdAndUserId(cartItem.getId(), user.getId())).willReturn(Optional.of(cartItem));

        //when
        cartService.deleteCartItem(cartItem.getId(), user.getId());

        //then
        verify(cartItemRepository).delete(cartItem);
    }
}
