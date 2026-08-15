package com.seungjig.shoppingmall.cart.service;

import com.seungjig.shoppingmall.cart.entity.Cart;
import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.cart.entity.dto.CartItemCreateDto;
import com.seungjig.shoppingmall.cart.entity.dto.CartListDto;
import com.seungjig.shoppingmall.cart.repository.CartItemRepository;
import com.seungjig.shoppingmall.cart.repository.CartRepository;
import com.seungjig.shoppingmall.global.util.FileService;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


   // 장바구니 상품 추가
   @Transactional
   public void createCartItem(CartItemCreateDto cartItemCreateDto, Long userId) {

       Cart cart = cartRepository.findByUserId(userId)
               .orElseGet(() -> {
                   User user = userRepository.findById(userId)
                           .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
                   return cartRepository.save(new Cart(user));
               });

       Product product = productRepository.findById(cartItemCreateDto.getProductId())
               .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

       Optional<CartItem> optionalCartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

       if (optionalCartItem.isPresent()) {
           optionalCartItem.get().changeQuantity(cartItemCreateDto.getItemQuantity());
       } else {
           cartItemRepository.save(
                   new CartItem(cart, product, cartItemCreateDto.getItemQuantity(), product.getPrice()));
       }

   }

   // 장바구니 목록 조회
    @Transactional
    public List<CartListDto> getCartList(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
                    return cartRepository.save(new Cart(user));
                });

        return cartItemRepository.findByCartIdWithProduct(cart.getId())
                .stream()
                .map(this::mapToCartListDto)
                .toList();

    }

    // 장바구니 상품 수량 변경
    @Transactional
    public void changeCartItemQuantity(Long cartItemId, Long userId, int cartItemQuantity) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 상품이거나 접근 권한이 없는 상품입니다."));

        if (cartItemQuantity < 1) {
            throw new IllegalArgumentException("장바구니 상품 수량은 최소 1개 이상이여야 합니다.");
        }

        cartItem.setQuantity(cartItemQuantity);
    }

    // 장바구니 상품 수량 조회
    @Transactional(readOnly = true)
    public Integer getCartItemQuantity(Long cartItemId) {
       return cartItemRepository.findById(cartItemId)
               .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 상품 입니다.")).getQuantity();
    }

    private CartListDto mapToCartListDto(CartItem cartItem) {
       return new CartListDto(cartItem.getId(), cartItem.getProduct().getId(),
               cartItem.getProduct().getName(), FileService.PRODUCT_IMAGE_PREFIX + cartItem.getProduct().getImageHeadUrl(),
               cartItem.getUnitPrice(), cartItem.getQuantity(), cartItem.getQuantity() * cartItem.getUnitPrice());
    }


}
