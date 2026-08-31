package com.seungjig.shoppingmall.cart.repository;

import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProduct(@Param("cartId") Long cartId);

    @Query("select ci from CartItem ci " +
            "join ci.cart c " +
            "where ci.id = :cartItemId and c.user.id = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("cartItemId") Long cartItemId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartItem ci where ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
}
