package com.seungjig.shoppingmall.order.repository;

import com.seungjig.shoppingmall.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
