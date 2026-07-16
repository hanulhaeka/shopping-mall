package com.seungjig.shoppingmall.cart.repository;

import com.seungjig.shoppingmall.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
