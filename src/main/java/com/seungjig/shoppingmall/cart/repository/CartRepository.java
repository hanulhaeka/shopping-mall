package com.seungjig.shoppingmall.cart.repository;

import com.seungjig.shoppingmall.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
