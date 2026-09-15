package com.seungjig.shoppingmall.order.repository;

import com.seungjig.shoppingmall.order.entity.Order;
import com.seungjig.shoppingmall.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.orderStatus = :toStatus " +
            "WHERE o.id = :orderId AND o.orderStatus =:fromStatus AND o.user.id = :userId")
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("fromStatus") OrderStatus fromStatus,
                          @Param("toStatus") OrderStatus toStatus,
                          @Param("userId") Long userId);
}
