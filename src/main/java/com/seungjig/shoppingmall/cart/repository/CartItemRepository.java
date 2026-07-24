package com.seungjig.shoppingmall.cart.repository;

import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
