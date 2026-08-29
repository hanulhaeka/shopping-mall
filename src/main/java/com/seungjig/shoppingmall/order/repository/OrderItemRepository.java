package com.seungjig.shoppingmall.order.repository;

import com.seungjig.shoppingmall.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
