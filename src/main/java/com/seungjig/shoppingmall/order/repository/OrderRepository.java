package com.seungjig.shoppingmall.order.repository;

import com.seungjig.shoppingmall.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("select o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product p " +
            "where o.id = :orderId and o.user.id = :userId")
    Optional<Order> findOrderWithItemsAndProducts(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
